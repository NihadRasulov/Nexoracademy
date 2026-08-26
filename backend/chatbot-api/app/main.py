"""
Nexora Academy AI Chatbot – application entry point.

Startup sequence:
  1. Load .env and settings
  2. Configure structured logging
  3. Connect to Redis (optional – degrades gracefully)
  4. Initialise RAG pipeline (optional)
  5. Initialise LLM client (with circuit breaker wired to Redis)
  6. Build Orchestrator and inject into routes
  7. Expose /health and /metrics endpoints
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

app = FastAPI(
    title="Nexora Academy AI",
    version="2.0.0",
    description="Production-grade conversational AI for Nexora Academy",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://nexoracademy.az"],
    allow_methods=["POST", "GET"],
    allow_headers=["Content-Type", "Accept"],
    allow_credentials=True,
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

orchestrator = Orchestrator(retriever=retriever, llm=llm)
init_chat(orchestrator)

app.include_router(chat_router, prefix="/api")

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

    return {
        "status": "ok",
        "service": "nexora-ai-chatbot",
        "version": "2.0.0",
        "environment": settings.environment,
        "components": {
            "llm": llm is not None,
            "llm_circuit": cb_state,
            "rag": retriever is not None,
            "rag_docs": retriever.count() if retriever else 0,
            "redis": redis_ok,
            "platform_api": settings.platform_api_url,
        },
    }

# ── Metrics endpoint (Prometheus) ─────────────────────────────────────────────

if settings.metrics_enabled:
    try:
        from prometheus_client import make_asgi_app
        metrics_app = make_asgi_app()
        app.mount("/metrics", metrics_app)
        logger.info("metrics_endpoint_mounted path=/metrics")
    except Exception as exc:
        logger.warning("metrics_mount_failed error=%s", exc)

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
