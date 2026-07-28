import os
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from dotenv import load_dotenv

dotenv_path = Path(__file__).resolve().parent / ".env"
load_dotenv(dotenv_path=dotenv_path)

app = FastAPI(title="Nexora Academy AI")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

from rag.embedder import get_embedding_function
from rag.retriever import Retriever
from rag.indexer import build_index
from llm.client import LLMClient
from core.orchestrator import Orchestrator
from routes.chat import router as chat_router, init as init_chat
from routes.lead import router as lead_router

llm = None
retriever = None

api_key = os.environ.get("OPENROUTER_API_KEY")
if api_key:
    print("[INIT] LLM initializing with OpenRouter")
    llm = LLMClient(api_key=api_key)

print("[INIT] Initializing RAG with local embeddings...")
try:
    embed_fn = get_embedding_function()
    retriever = Retriever(embed_fn=embed_fn, persist_dir="./chroma_db")
    build_index(retriever)
except Exception as e:
    print(f"[WARN] Embedding/RAG init failed ({e}). Running without RAG.")
    retriever = None

if llm is None:
    print("[WARN] No API keys found. LLM and RAG will be unavailable.")

orchestrator = Orchestrator(retriever=retriever, llm=llm)
init_chat(orchestrator)

app.include_router(chat_router, prefix="/api")
app.include_router(lead_router, prefix="/api")


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "service": "nexora-ai-chatbot",
        "llm": llm is not None,
        "rag": retriever is not None,
        "documents": retriever.count() if retriever else 0,
    }


# Serve frontend from Spring Boot static resources (or fallback to public/)
FRONTEND_DIR = Path(__file__).resolve().parent.parent.parent / "src" / "main" / "resources" / "static"
if not FRONTEND_DIR.exists():
    FRONTEND_DIR = Path(__file__).resolve().parent.parent / "public"
if FRONTEND_DIR.exists():
    app.mount("/", StaticFiles(directory=str(FRONTEND_DIR), html=True), name="frontend")
    print(f"[INIT] Serving frontend from {FRONTEND_DIR}")

if __name__ == "__main__":
    import uvicorn
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
