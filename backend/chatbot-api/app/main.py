"""
Nexora Academy AI Chatbot – application entry point.

Startup sequence:
  1. Load .env and settings
  2. Configure structured logging
  3. Connect to Redis (optional – degrades gracefully)
  4. Initialise database (SQLite default / PostgreSQL in production)
  5. Initialise RAG pipeline (optional)
  6. Initialise LLM client (with circuit breaker wired to Redis)
  7. Build Orchestrator and inject into routes
  8. Mount static frontend (if present)
  9. Expose /health and /metrics endpoints
"""
from __future__ import annotations

import logging
import os
import sys
from pathlib import Path

# ── stdout encoding (Windows compatibility) ──────────────────────────────────
for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

# ── Python path ──────────────────────────────────────────────────────────────
sys.path.insert(0, str(Path(__file__).resolve().parent))

# ── Dotenv ───────────────────────────────────────────────────────────────────
from dotenv import load_dotenv

dotenv_path = Path(__file__).resolve().parent / ".env"
load_dotenv(dotenv_path=dotenv_path)

# ── Config (must be imported AFTER dotenv is loaded) ─────────────────────────
from core.config import settings

# ── Logging ──────────────────────────────────────────────────────────────────
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format='{"time":"%(asctime)s","level":"%(levelname)s","logger":"%(name)s","msg":%(message)s}',
    datefmt="%Y-%m-%dT%H:%M:%S",
)
logger = logging.getLogger("nexora.main")

# Validate config on startup
for warning in settings.validate():
    logger.warning("config_warning msg=%r", warning)

# ── FastAPI app ───────────────────────────────────────────────────────────────
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

app = FastAPI(
    title="Nexora Academy AI",
    version="2.0.0",
    description="Production-grade conversational AI for Nexora Academy",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://nexoracademy.az", "https://www.nexoracademy.az"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Redis ─────────────────────────────────────────────────────────────────────
_redis_client = None

if settings.redis_enabled:
    try:
        import redis as redis_lib
        _redis_client = redis_lib.from_url(
            settings.redis_url,
            decode_responses=True,
            socket_connect_timeout=3,
            socket_timeout=3,
        )
        _redis_client.ping()
        logger.info("redis_connected url=%s", settings.redis_url.split("@")[-1])
    except Exception as exc:
        logger.warning("redis_unavailable error=%s – falling back to in-memory", exc)
        _redis_client = None

# Wire Redis into session manager and rate limiter
from core.session import set_manager, SessionManager
from core.rate_limiter import set_redis as set_rate_limiter_redis

_session_manager = SessionManager(redis_client=_redis_client)
set_manager(_session_manager)
set_rate_limiter_redis(_redis_client)

# ── Database ──────────────────────────────────────────────────────────────────
try:
    from db.database import init_db
    init_db()
    logger.info("database_ready url=%s", settings.database_url.split("@")[-1] if "@" in settings.database_url else settings.database_url)
except Exception as exc:
    logger.error("database_init_failed error=%s – leads will buffer in memory", exc)

# ── RAG pipeline ──────────────────────────────────────────────────────────────
retriever = None

logger.info("rag_init_start")
try:
    from rag.embedder import get_embedding_function
    from rag.retriever import Retriever
    from rag.indexer import build_index

    embed_fn = get_embedding_function()
    retriever = Retriever(embed_fn=embed_fn, persist_dir="./chroma_db")
    build_index(retriever)
    logger.info("rag_ready docs=%d", retriever.count() if retriever else 0)
except Exception as exc:
    logger.warning("rag_init_failed error=%s – running without RAG", exc)
    retriever = None

# ── LLM client ────────────────────────────────────────────────────────────────
llm = None

if settings.llm_available:
    try:
        from llm.client import LLMClient
        llm = LLMClient(
            api_key=settings.openrouter_api_key,
            model=settings.openrouter_model,
            redis_client=_redis_client,   # circuit breaker state in Redis
        )
        logger.info("llm_ready model=%s", settings.openrouter_model)
    except Exception as exc:
        logger.error("llm_init_failed error=%s – running without LLM", exc)
        llm = None
else:
    logger.warning("llm_skipped – OPENROUTER_API_KEY not set")

# ── Orchestrator ──────────────────────────────────────────────────────────────
from core.orchestrator import Orchestrator
from routes.chat import router as chat_router, init as init_chat
from routes.lead import router as lead_router

orchestrator = Orchestrator(retriever=retriever, llm=llm)
init_chat(orchestrator)

app.include_router(chat_router, prefix="/api")
app.include_router(lead_router, prefix="/api")

# ── Global error handlers ─────────────────────────────────────────────────────

from fastapi import Request as _Request
from fastapi.responses import JSONResponse

@app.exception_handler(Exception)
async def unhandled_exception_handler(_req: _Request, exc: Exception):
    logger.error("unhandled_exception type=%s error=%s", type(exc).__name__, exc)
    return JSONResponse(
        status_code=500,
        content={
            "reply": "Texniki problem baş verdi. Zəhmət olmasa bir az gözlə və yenidən cəhd et.",
            "state": "error",
            "actions": [],
            "courses": [],
            "capture": "none",
        },
    )

from fastapi.exceptions import RequestValidationError

@app.exception_handler(RequestValidationError)
async def validation_error_handler(_req: _Request, exc: RequestValidationError):
    logger.warning("validation_error detail=%s", exc.errors())
    return JSONResponse(
        status_code=422,
        content={
            "reply": "Sorğunuz düzgün formatda deyil. Zəhmət olmasa yenidən cəhd edin.",
            "state": "error",
            "detail": exc.errors(),
        },
    )

# ── Health endpoint ───────────────────────────────────────────────────────────

@app.get("/health")
async def health():
    from db.database import health_check as db_health

    redis_ok = False
    if _redis_client:
        try:
            _redis_client.ping()
            redis_ok = True
        except Exception:
            redis_ok = False

    cb_state = "unknown"
    if llm:
        try:
            cb_state = llm.cb.state()
        except Exception:
            pass

    db_ok = db_health() if isinstance(db_health(), bool) else db_health().get("healthy", True)
    all_ok = redis_ok and db_ok
    status_code = 200 if all_ok else 503
    from fastapi.responses import JSONResponse as _JSONResponse
    return _JSONResponse(
        status_code=status_code,
        content={
            "status": "ok" if all_ok else "degraded",
            "service": "nexora-ai-chatbot",
            "version": "2.0.0"
        }
    )

# ── Metrics endpoint (Prometheus) ─────────────────────────────────────────────

if settings.metrics_enabled:
    try:
        from prometheus_client import make_asgi_app
        metrics_app = make_asgi_app()
        app.mount("/metrics", metrics_app)
        logger.info("metrics_endpoint_mounted path=/metrics")
    except Exception as exc:
        logger.warning("metrics_mount_failed error=%s", exc)

# ── Static frontend ───────────────────────────────────────────────────────────

FRONTEND_DIR = Path(__file__).resolve().parent.parent.parent / "src" / "main" / "resources" / "static"
if not FRONTEND_DIR.exists():
    FRONTEND_DIR = Path(__file__).resolve().parent.parent / "public"
if FRONTEND_DIR.exists():
    app.mount("/chat", StaticFiles(directory=str(FRONTEND_DIR), html=True), name="frontend")
    logger.info("frontend_mounted path=%s mount=/chat", FRONTEND_DIR)

# ── Dev server entry point ────────────────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.port,
        reload=not settings.is_production,
        log_level=settings.log_level.lower(),
    )
