from __future__ import annotations
from typing import Optional

sessions: dict[str, dict] = {}


def get_or_create(session_id: str) -> dict:
    if session_id not in sessions:
        sessions[session_id] = {
            "state": "start",
            "data": {"interest": None, "level": None, "name": None, "phone": None},
            "history": [],
        }
    return sessions[session_id]


def update_state(session: dict, state: str):
    session["state"] = state


def add_history(session: dict, role: str, text: str):
    session["history"].append({"role": role, "text": text})
    if len(session["history"]) > 50:
        session["history"] = session["history"][-50:]


def reset(session: dict):
    session["state"] = "start"
    session["data"] = {"interest": None, "level": None, "name": None, "phone": None}
