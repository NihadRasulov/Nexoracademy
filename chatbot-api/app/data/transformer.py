from __future__ import annotations

import re


def _clean(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()


def course_to_documents(course: dict) -> list[dict]:
    docs = []

    overview = _clean(
        f"Course: {course['title']}. "
        f"Direction: {course.get('direction', '')}. "
        f"Level: {course.get('level', '')}. "
        f"Format: {course.get('format', '')}. "
        f"Duration: {course.get('durationWeeks', '')} weeks. "
        f"Price: {course.get('priceAzn', '')} AZN. "
        f"Description: {course.get('shortDescription', '')}. "
        f"Tools: {', '.join(course.get('tools', []))}. "
        f"Instructor: {course.get('instructor', {}).get('name', '')} - "
        f"{course.get('instructor', {}).get('title', '')}. "
        f"Mentor: {course.get('mentor', {}).get('name', '')} - "
        f"{course.get('mentor', {}).get('role', '')}. "
        f"Schedule: {course.get('schedule', {}).get('days', '')} "
        f"at {course.get('schedule', {}).get('time', '')}. "
        f"Next cohort starts: {course.get('cohort', {}).get('startDate', '')}. "
        f"Target audience: {', '.join(course.get('audience', []))}."
    )
    docs.append({
        "id": f"{course['id']}_overview",
        "text": overview,
        "metadata": {"source": "course", "course_id": course["id"], "type": "overview"},
    })

    for mod in course.get("syllabus", []):
        module_text = _clean(
            f"Module: {mod.get('module', '')} (Weeks {mod.get('weeks', '')}). "
            f"{mod.get('description', '')} "
            f"Topics: {', '.join(mod.get('subtopics', []))}."
        )
        docs.append({
            "id": f"{course['id']}_syllabus_{mod.get('module', '')[:20]}",
            "text": module_text,
            "metadata": {"source": "syllabus", "course_id": course["id"], "type": "module"},
        })

    for faq in course.get("faq", []):
        faq_text = _clean(f"Q: {faq['question']} A: {faq['answer']}")
        docs.append({
            "id": f"{course['id']}_faq_{faq['question'][:30]}",
            "text": faq_text,
            "metadata": {"source": "faq", "course_id": course["id"], "type": "faq"},
        })

    return docs


def knowledge_to_documents(entries: list[dict]) -> list[dict]:
    docs = []
    for entry in entries:
        text = _clean(f"Topic: {', '.join(entry.get('keywords', []))}. {entry['text']}")
        docs.append({
            "id": entry["id"],
            "text": text,
            "metadata": {"source": "knowledge_base", "type": "faq"},
        })
    return docs


def reviews_to_documents(reviews: list[dict], courses: list[dict]) -> list[dict]:
    course_map = {c["id"]: c["title"] for c in courses}
    docs = []
    for r in reviews:
        title = course_map.get(r.get("courseId", ""), "")
        text = _clean(f"Review for {title}: {r.get('text', '')} Rating: {r.get('rating', '')}/5")
        docs.append({
            "id": r["id"],
            "text": text,
            "metadata": {"source": "review", "course_id": r.get("courseId", ""), "type": "review"},
        })
    return docs


def build_all_documents(data: dict) -> list[dict]:
    docs = []
    for course in data.get("courses", []):
        docs.extend(course_to_documents(course))
    docs.extend(knowledge_to_documents(data.get("knowledge", [])))
    docs.extend(reviews_to_documents(data.get("reviews", []), data.get("courses", [])))
    return docs
