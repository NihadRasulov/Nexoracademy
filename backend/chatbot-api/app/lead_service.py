"""
Lead service – persists leads to the database with in-memory fallback.
No PII is logged in plaintext.
"""
from __future__ import annotations

import logging
import secrets
import time

logger = logging.getLogger("nexora.lead_service")

_fallback_leads: list[dict] = []


def _make_ref() -> str:
    ts = int(time.time())
    rand = secrets.token_hex(4)
    return f"lead-{ts}-{rand}"


def add_lead(data: dict) -> dict:
    lead_ref = _make_ref()
    entry = {
        "id": lead_ref,
        "name": data.get("name", ""),
        "phone": data.get("phone", ""),
        "email": data.get("email", ""),
        "interest": data.get("interest", ""),
        "level": data.get("level", ""),
        "note": data.get("note", ""),
        "source": data.get("source", "chatbot"),
        "sessionId": data.get("sessionId", ""),
        "createdAt": __import__("datetime").datetime.now().isoformat(),
    }

    try:
        from db.database import get_db
        from db.models import Lead

        with get_db() as db:
            lead_row = Lead(
                lead_ref=lead_ref,
                name=entry["name"],
                phone=entry["phone"],
                email=entry["email"] or None,
                interest=entry["interest"] or None,
                level=entry["level"] or None,
                note=entry["note"] or None,
                source=entry["source"],
                session_id=entry["sessionId"] or None,
            )
            db.add(lead_row)
        logger.info("lead_saved ref=%s source=%s", lead_ref, entry["source"])
        return entry
    except Exception as exc:
        logger.error("lead_db_error ref=%s error=%s – buffering in memory", lead_ref, exc)
        _fallback_leads.append(entry)
        return entry


def get_leads_page(limit: int = 20, offset: int = 0) -> list[dict]:
    results: list[dict] = []

    try:
        from db.database import get_db
        from db.models import Lead

        with get_db() as db:
            rows = (
                db.query(Lead)
                .order_by(Lead.created_at.desc())
                .offset(offset)
                .limit(limit)
                .all()
            )
            results = [r.to_dict() for r in rows]
    except Exception as exc:
        logger.warning("lead_list_db_error error=%s", exc)

    return results


def get_all_leads() -> list[dict]:
    return get_leads_page(limit=500, offset=0)


def count_leads() -> int:
    try:
        from db.database import get_db
        from db.models import Lead

        with get_db() as db:
            count = db.query(Lead).count()
        return count + len(_fallback_leads)
    except Exception:
        return len(_fallback_leads)
