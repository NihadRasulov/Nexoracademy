"""
Session management with Redis persistence and in-memory fallback.
Server-issued session IDs are used instead of trusting caller-supplied values.
"""
from __future__ import annotations

import json
import logging
import secrets
import time
import re

from core.config import settings

logger = logging.getLogger("nexora.session")

_mem_sessions: dict[str, dict] = {}

MAX_IN_MEMORY_SESSIONS = 5000

SESSION_ID_PATTERN = re.compile(r'^[a-zA-Z0-9_-]{16,128}$')


def generate_session_id() -> str:
    return secrets.token_urlsafe(24)


def _validate_session_id(session_id: str) -> str | None:
    if session_id and SESSION_ID_PATTERN.match(session_id):
        return session_id
    return None


def _session_key(session_id: str) -> str:
    return f"session:{session_id}"


def _fresh_session(session_id: str) -> dict:
    now = time.time()
    return {
        "sessionId": session_id,
        "state": "start",
        "data": {
            "interest": None,
            "level": None,
            "direction": None,
            "selected_course": None,
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

    def get_or_create(self, session_id: str) -> dict:
        validated = _validate_session_id(session_id)

        if validated:
            session = self._load(validated)
            if session is not None:
                return session

        new_id = generate_session_id()
        session = _fresh_session(new_id)
        logger.info("session_created session_id=%s", new_id)
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
            "direction": None,
            "selected_course": None,
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

        if len(_mem_sessions) >= MAX_IN_MEMORY_SESSIONS:
            oldest_keys = sorted(
                _mem_sessions.keys(),
                key=lambda k: _mem_sessions[k].get("updatedAt", 0)
            )[:MAX_IN_MEMORY_SESSIONS // 2]
            for k in oldest_keys:
                del _mem_sessions[k]

        _mem_sessions[session["sessionId"]] = session


_manager: SessionManager = SessionManager(redis_client=None)


def set_manager(manager: SessionManager):
    global _manager
    _manager = manager


def get_or_create(session_id: str, user_id: str | None = None) -> dict:
    return _manager.get_or_create(session_id)


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
