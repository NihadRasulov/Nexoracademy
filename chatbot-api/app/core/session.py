"""
Session management with Redis persistence and in-memory fallback.
"""
from __future__ import annotations

import json
import logging
import time
from typing import Optional

from core.config import settings

logger = logging.getLogger("nexora.session")

_mem_sessions: dict[str, dict] = {}


def _session_key(session_id: str) -> str:
    return f"session:{session_id}"


def _fresh_session(session_id: str, user_id: str | None = None) -> dict:
    now = time.time()
    return {
        "sessionId": session_id,
        "userId": user_id,
        "state": "start",
        "data": {
            "interest": None,
            "level": None,
            "name": None,
            "phone": None,
        },
        "history": [],
        "createdAt": now,
        "updatedAt": now,
        "turnCount": 0,
    }


class SessionManager:
    def __init__(self, redis_client=None):
        self.redis = redis_client

    def get_or_create(self, session_id: str, user_id: str | None = None) -> dict:
        session = self._load(session_id)

        if session is None:
            session = _fresh_session(session_id, user_id)
            logger.info("session_created session_id=%s user_id=%s", session_id, user_id)
        else:
            if user_id and not session.get("userId"):
                session["userId"] = user_id

        return session

    def save(self, session: dict):
        session["updatedAt"] = time.time()
        session["turnCount"] = session.get("turnCount", 0) + 1

        max_entries = settings.session_history_max * 2
        if len(session["history"]) > max_entries:
            session["history"] = session["history"][-max_entries:]

        self._store(session)

    def add_turn(self, session: dict, user_msg: str, assistant_reply: str):
        session["history"].append({"role": "user", "text": user_msg})
        session["history"].append({"role": "assistant", "text": assistant_reply})

    def get_context_history(self, session: dict) -> list[dict]:
        context_entries = settings.session_history_context * 2
        return [
            {"role": h["role"], "content": h["text"]}
            for h in session["history"][-context_entries:]
        ]

    def reset(self, session: dict):
        session["state"] = "start"
        session["data"] = {
            "interest": None,
            "level": None,
            "name": None,
            "phone": None,
        }
        session["history"] = []

    def _load(self, session_id: str) -> dict | None:
        key = _session_key(session_id)

        if self.redis:
            try:
                raw = self.redis.get(key)
                if raw:
                    return json.loads(raw)
                return None
            except Exception as exc:
                logger.warning("redis_read_error key=%s error=%s", key, exc)

        return _mem_sessions.get(session_id)

    def _store(self, session: dict):
        key = _session_key(session["sessionId"])
        serialised = json.dumps(session, ensure_ascii=False)

        if self.redis:
            try:
                self.redis.setex(key, settings.session_ttl_s, serialised)
                return
            except Exception as exc:
                logger.warning("redis_write_error key=%s error=%s", key, exc)

        _mem_sessions[session["sessionId"]] = session


_manager: SessionManager = SessionManager(redis_client=None)


def set_manager(manager: SessionManager):
    global _manager
    _manager = manager


def get_or_create(session_id: str, user_id: str | None = None) -> dict:
    return _manager.get_or_create(session_id, user_id)


def save(session: dict):
    _manager.save(session)


def update_state(session: dict, state: str):
    session["state"] = state


def add_history(session: dict, role: str, text: str):
    session["history"].append({"role": role, "text": text})
    max_entries = settings.session_history_max * 2
    if len(session["history"]) > max_entries:
        session["history"] = session["history"][-max_entries:]


def reset(session: dict):
    _manager.reset(session)


def get_context_history(session: dict) -> list[dict]:
    return _manager.get_context_history(session)
