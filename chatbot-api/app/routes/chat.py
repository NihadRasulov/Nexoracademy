import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

from fastapi import APIRouter, Request
from fastapi.responses import StreamingResponse
from models.schemas import ChatRequest
from core.orchestrator import Orchestrator
from core.rate_limiter import is_rate_limited

router = APIRouter()
_orchestrator: Orchestrator | None = None


def init(orchestrator: Orchestrator):
    global _orchestrator
    _orchestrator = orchestrator


@router.post("/chat")
async def chat(req: ChatRequest, request: Request):
    client_ip = request.client.host if request.client else "unknown"
    if is_rate_limited(client_ip):
        return {
            "reply": "Bir az yavaş yaz, nəfəs al Mən də sənə çatım! Bir az gözlə, yenə yaza bilərsən.",
            "state": "rate_limited",
            "actions": [],
            "courses": [],
            "capture": "none",
        }

    session_id = req.conversationId or req.sessionId or client_ip

    print(f"\n=== CHAT REQUEST ===")
    print(f"IP: {client_ip}")
    print(f"MESSAGE: {req.message}")
    print(f"SESSION: {session_id}")

    result = _orchestrator.process(req.message, session_id)

    reply_preview = result.reply[:100] if result.reply else ""
    print(f"REPLY: {reply_preview}")
    print(f"STATE: {result.state}")
    print(f"ACTIONS: {len(result.actions)}")
    print(f"=== END ===\n")

    use_stream = request.headers.get("accept", "").find("text/event-stream") >= 0
    if use_stream:
        return _stream_response(result.reply)
    return result.model_dump()


def _stream_response(text: str):
    async def generate():
        words = text.split(" ")
        for i, word in enumerate(words):
            yield f"data: {word}\n\n"
            import asyncio
            await asyncio.sleep(0.05)
        yield "data: [DONE]\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")
