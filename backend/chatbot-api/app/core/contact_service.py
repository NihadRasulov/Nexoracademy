"""Send chatbot contact requests to the main Java platform."""
from __future__ import annotations

import logging

import httpx

from core.config import settings

logger = logging.getLogger("nexora.contact_service")


def submit_contact(*, name: str, phone: str, interest: str, level: str) -> bool:
    message_parts = ["Chatbot üzərindən kurs məlumatı istəyi."]
    if interest:
        message_parts.append(f"Maraq sahəsi: {interest}.")
    if level:
        message_parts.append(f"Səviyyə: {level}.")

    try:
        response = httpx.post(
            f"{settings.platform_api_url}/api/v1/public/contact-submissions",
            json={
                "fullName": name,
                "email": None,
                "phone": phone,
                "message": " ".join(message_parts),
                "courseId": None,
            },
            timeout=5.0,
        )
        response.raise_for_status()
        logger.info("chatbot_contact_saved")
        return True
    except (httpx.HTTPError, ValueError) as exc:
        logger.error("chatbot_contact_failed error=%s", exc)
        return False
