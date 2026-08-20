"""
Chat route – production-grade rewrite.
Server-issues session IDs; caller-supplied userId is not trusted.
"""
from __future__ import annotations

import sys
import logging

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except (AttributeError, ValueError):
    pass

from fastapi import APIRouter, Request
from fastapi.responses import StreamingResponse

from models.schemas import ChatRequest
from core.orchestrator import Orchestrator
from core.rate_limiter import is_rate_limited
from core import session as session_mod

logger = logging.getLogger("nexora.route.chat")

router = APIRouter()
_orchestrator: Orchestrator | None = None


def init(orchestrator: Orchestrator):
    global _orchestrator
    _orchestrator = orchestrator


@router.post("/chat")
async def chat(req: ChatRequest, request: Request):
    client_ip = request.client.host if request.client else "unknown"

    raw_session = req.conversationId or req.sessionId or ""
    sess = session_mod.get_or_create(raw_session)
    session_id = sess["sessionId"]

    if is_rate_limited(client_ip):
        logger.info("rate_limited session=%s ip=%s", session_id, client_ip)
        return {
            "reply": (
                "Bir az yavaş yaz, nəfəs al — mən də sənə çatım! "
                "Bir az gözlə, yenə yaza bilərsən."
            ),
            "state": "rate_limited",
            "actions": [],
            "courses": [],
            "capture": "none",
            "sessionId": session_id,
        }

    message = (req.message or "").strip()
    if len(message) > 2000:
        message = message[:2000]

    logger.info("chat_request session=%s ip=%s msg_len=%d", session_id, client_ip, len(message))

    if _orchestrator is None:
        logger.error("orchestrator_not_initialised")
        return {
            "reply": "Sistem hazırlanır. Zəhmət olmasa bir az gözlə.",
            "state": "error",
            "actions": [],
            "courses": [],
            "capture": "none",
            "sessionId": session_id,
        }

    result = _orchestrator.process(message, session_id)

    logger.info("chat_response session=%s state=%s actions=%d", session_id, result.state, len(result.actions))

    response = result.model_dump()
    response["sessionId"] = session_id

    accept = request.headers.get("accept", "")
    if "text/event-stream" in accept:
        return _stream_response(result.reply, session_id)

    return response


def _stream_response(text: str, session_id: str) -> StreamingResponse:
    async def generate():
        import asyncio
        yield f"data: {__import__('json').dumps({'sessionId': session_id})}\n\n"
        words = text.split(" ")
        for word in words:
            yield f"data: {word}\n\n"
            await asyncio.sleep(0.04)
        yield "data: [DONE]\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")
