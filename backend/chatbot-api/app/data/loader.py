from __future__ import annotations

import json
import logging
import threading
import time
from pathlib import Path

import httpx

from core.config import settings


DATA_DIR = Path(__file__).resolve().parent.parent / "data_files"
logger = logging.getLogger("nexora.data.loader")

_catalog_lock = threading.Lock()
_catalog_cache: dict[str, object] = {"expires_at": 0.0, "courses": None}

_LEVEL_LABELS = {
    "BEGINNER": "Başlanğıc",
    "INTERMEDIATE": "Orta",
    "ADVANCED": "İrəli",
    "beginner": "Başlanğıc",
    "intermediate": "Orta",
    "advanced": "İrəli",
}


def _load_json(filename: str) -> list[dict]:
    path = DATA_DIR / filename
    if not path.exists():
        return []
    with path.open(encoding="utf-8") as file:
        value = json.load(file)
    return value if isinstance(value, list) else []


def _as_list(value: object) -> list:
    if isinstance(value, list):
        return value
    if isinstance(value, str) and value.strip():
        return [item.strip() for item in value.split(",") if item.strip()]
    return []


def _normalise_platform_course(raw: dict, category_names: dict[str, str]) -> dict:
    """Adapt Java's public catalog DTO to the stable shape used by the chatbot."""
    content = raw.get("content") if isinstance(raw.get("content"), dict) else {}
    instructor_raw = raw.get("instructor") if isinstance(raw.get("instructor"), dict) else {}
    category_id = raw.get("categoryId")
    direction = content.get("direction") or category_names.get(str(category_id), "")
    schedule = content.get("schedule") if isinstance(content.get("schedule"), dict) else {}

    price = raw.get("basePrice")
    try:
        price = float(price) if price is not None else None
        if price is not None and price.is_integer():
            price = int(price)
    except (TypeError, ValueError):
        price = None

    audience = content.get("audience", raw.get("targetAudience"))
    return {
        "id": str(raw.get("id") or raw.get("slug") or ""),
        "title": str(raw.get("title") or ""),
        "direction": str(direction or ""),
        "format": str(raw.get("deliveryFormat") or "").lower(),
        "level": _LEVEL_LABELS.get(str(raw.get("difficulty") or ""), str(raw.get("difficulty") or "")),
        "durationWeeks": raw.get("durationWeeks"),
        "priceAzn": price,
        "shortDescription": str(raw.get("shortDescription") or ""),
        "tools": _as_list(content.get("tools")),
        "instructor": {
            "name": str(instructor_raw.get("fullName") or ""),
            "title": str(instructor_raw.get("bio") or ""),
            "rating": instructor_raw.get("avgRating"),
        },
        "mentor": content.get("mentor") if isinstance(content.get("mentor"), dict) else {},
        "syllabus": content.get("syllabus") if isinstance(content.get("syllabus"), list) else [],
        "faq": content.get("faq") if isinstance(content.get("faq"), list) else [],
        "audience": _as_list(audience),
        "schedule": {
            "days": str(schedule.get("days") or content.get("scheduleDays") or ""),
            "time": str(schedule.get("time") or content.get("scheduleTime") or ""),
        },
        "cohort": content.get("cohort") if isinstance(content.get("cohort"), dict) else {},
    }


def _fetch_platform_courses() -> list[dict]:
    base_url = settings.platform_api_url
    with httpx.Client(timeout=settings.catalog_timeout_s) as client:
        category_response = client.get(f"{base_url}/api/v1/public/catalog/categories")
        category_response.raise_for_status()
        category_payload = category_response.json()
        category_names = {
            str(item.get("id")): str(item.get("name") or "")
            for item in category_payload
            if isinstance(item, dict) and item.get("id") is not None
        }

        page = 0
        raw_courses: list[dict] = []
        while True:
            response = client.get(
                f"{base_url}/api/v1/public/catalog/courses",
                params={"page": page, "size": 100, "sort": "title,asc"},
            )
            response.raise_for_status()
            payload = response.json()
            if not isinstance(payload, dict):
                raise ValueError("Java catalog returned an invalid page")
            raw_courses.extend(item for item in payload.get("content", []) if isinstance(item, dict))
            total_pages = int(payload.get("totalPages") or 0)
            page += 1
            if page >= max(total_pages, 1):
                break

    return [_normalise_platform_course(course, category_names) for course in raw_courses]


def load_courses() -> list[dict]:
    now = time.monotonic()
    with _catalog_lock:
        cached = _catalog_cache["courses"]
        if isinstance(cached, list) and now < float(_catalog_cache["expires_at"]):
            return cached

    try:
        courses = _fetch_platform_courses()
        if not courses:
            raise ValueError("Java catalog is empty")
        ttl = settings.catalog_cache_ttl_s
    except (httpx.HTTPError, ValueError, TypeError) as exc:
        logger.warning("platform_catalog_unavailable fallback=static error=%s", exc)
        courses = _load_json("courses.json")
        ttl = min(settings.catalog_cache_ttl_s, 30)

    with _catalog_lock:
        _catalog_cache["courses"] = courses
        _catalog_cache["expires_at"] = now + max(ttl, 1)
    return courses


def load_knowledge() -> list[dict]:
    return _load_json("knowledge.json")


def load_reviews() -> list[dict]:
    return _load_json("reviews.json")


def load_all_data() -> dict:
    return {
        "courses": load_courses(),
        "knowledge": load_knowledge(),
        "reviews": load_reviews(),
    }
