"""
Personalisation context injector.
"""
from __future__ import annotations

import logging

logger = logging.getLogger("nexora.personalisation")


class PersonalisationInjector:
    def build(self, session: dict) -> str:
        parts: list[str] = []
        data = session.get("data", {})
        user_id = session.get("userId")

        if data.get("name"):
            parts.append(f"İstifadəçinin adı: {data['name']}")
        elif user_id and user_id not in ("anonymous", ""):
            parts.append(f"İstifadəçi ID: {user_id}")

        if data.get("interest"):
            from core.orchestrator import DIRECTION_MAP
            label = DIRECTION_MAP.get(data["interest"], data["interest"])
            parts.append(f"Maraq sahəsi: {label}")

        if data.get("level"):
            from core.orchestrator import LEVEL_MAP
            label = LEVEL_MAP.get(data["level"], data["level"])
            parts.append(f"Hal-hazırki səviyyə: {label}")

        turn_count = session.get("turnCount", 0)
        if turn_count >= 10:
            parts.append("Aktiv söhbət aparılan istifadəçidir (10+ mesaj).")
        elif turn_count == 0:
            parts.append("İstifadəçi ilk dəfə müraciət edir.")

        if data.get("phone"):
            parts.append("Bu istifadəçi artıq qeydiyyatdan keçib.")

        if not parts:
            return ""

        return "İstifadəçi haqqında məlumat:\n" + "\n".join(f"  • {p}" for p in parts)

    def should_skip_greeting(self, session: dict) -> bool:
        return session.get("turnCount", 0) > 0
