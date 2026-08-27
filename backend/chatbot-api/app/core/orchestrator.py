"""
Conversation orchestrator – algorithmic guided flow.
Simple state machine, no LLM dependency for primary flow.
"""
from __future__ import annotations

import logging
from difflib import SequenceMatcher

from core.session import (
    get_or_create,
    save as session_save,
    update_state,
    add_history,
    reset,
)
from core.intent import extract_name
from core.extractor import extract_phone as extract_phone_clean
from data.loader import load_courses, load_knowledge
from models.schemas import ChatResponse, ActionButton, CourseCard
from core.observability import telemetry, TurnMetrics

logger = logging.getLogger("nexora.orchestrator")

CONTACT_INFO = (
    "Dəstək komandamız sənə kömək edə bilər:\n"
    "📞 +994 12 555 00 00\n"
    "📧 info@nexora.az\n"
    "📍 Bakı, Nərimanov, Əhməd Rəcəbli 23\n"
    "⏰ B.e - Şənbə, 10:00 - 20:00"
)

DIRECTION_LABELS = {
    "proqramlashdirma": "Proqramlaşdırma",
    "kiber": "Kibertəhlükəsizlik",
    "infra": "IT İnfrastruktur",
}

DIRECTION_SYNONYMS = {
    "proqramlashdirma": ["proqram", "kod", "kodlama", "developer", "veb", "frontend", "backend", "fullstack", "python", "flutter", "mobile", "tətbiq", "tətbiqetmə", "data", "analitika", "ui", "ux", "dizayn"],
    "kiber": ["kiber", "təhlükəsizlik", "hacker", "pentest", "soc", "şəbəkə", "guvənlik", "etik", "insident"],
    "infra": ["infra", "devops", "bulud", "cloud", "aws", "docker", "kubernetes", "ci/cd", "server"],
}


class Orchestrator:
    def __init__(self, retriever=None):
        self._courses = load_courses()
        self._knowledge = load_knowledge()
        self._build_direction_index()

    def _build_direction_index(self):
        self._directions: dict[str, list[dict]] = {}
        for course in self._courses:
            d = course.get("direction", "")
            if d not in self._directions:
                self._directions[d] = []
            self._directions[d].append(course)

        self._dir_key_by_label = {}
        for key, label in DIRECTION_LABELS.items():
            self._dir_key_by_label[label.lower()] = key
            self._dir_key_by_label[key] = key

    def process(self, message: str, session_id: str, user_id: str | None = None) -> ChatResponse:
        session = get_or_create(session_id, user_id)
        text = message.strip()[:2000]
        state_before = session["state"]

        logger.debug("turn_start session=%s state=%s user=%s", session_id, state_before, user_id)

        if not text or text in ("/start", ""):
            result = self._handle_start(session)
            self._finalize(session, text, result, state_before)
            return result

        add_history(session, "user", text)
        state = session["state"]

        if state == "start":
            result = self._handle_start(session, text)
        elif state == "direction_selected":
            result = self._handle_direction(session, text)
        elif state == "course_selected":
            result = self._handle_course_action(session, text)
        elif state == "faq":
            result = self._handle_faq(session, text)
        elif state == "register_name":
            result = self._handle_name(session, text)
        elif state == "register_phone":
            result = self._handle_phone(session, text)
        elif state == "completed":
            result = self._handle_completed(session, text)
        else:
            result = self._handle_fallback(session)

        self._finalize(session, text, result, state_before)
        return result

    def _handle_start(self, session: dict, text: str = "") -> ChatResponse:
        if text:
            lower = text.lower().strip()
            greeting_words = ["salam", "hi", "hello", "hey", "merhaba", "sağol", "sagol"]

            for key, label in DIRECTION_LABELS.items():
                if key == lower or label.lower() == lower:
                    session["data"]["direction"] = key
                    update_state(session, "direction_selected")
                    return self._handle_direction(session, text)

            for key, synonyms in DIRECTION_SYNONYMS.items():
                for syn in synonyms:
                    if syn in lower:
                        session["data"]["direction"] = key
                        update_state(session, "direction_selected")
                        return self._handle_direction(session, text)

            if lower not in greeting_words:
                return self._try_answer_question(session, text)

        update_state(session, "direction_selected")
        name = session.get("data", {}).get("name")
        greeting = f"Xoş gördük, {name}!" if name else "Salam!"

        return ChatResponse(
            reply=(
                f"{greeting} Nexora Academy kursları haqqında məlumat almaq istəyirsən. "
                "Hansı istiqamət səni daha çox maraqlandırır?"
            ),
            state="direction_selected",
            actions=[
                ActionButton(type="button", label="Proqramlaşdırma", value="proqramlashdirma"),
                ActionButton(type="button", label="Kibertəhlükəsizlik", value="kiber"),
                ActionButton(type="button", label="IT İnfrastruktur", value="infra"),
            ],
            courses=[],
            capture="none",
        )

    def _handle_direction(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower().strip()
        matched_key = None

        for key, label in DIRECTION_LABELS.items():
            if key == lower or label.lower() == lower:
                matched_key = key
                break

        if not matched_key:
            for key, synonyms in DIRECTION_SYNONYMS.items():
                for syn in synonyms:
                    if syn in lower:
                        matched_key = key
                        break
                if matched_key:
                    break

        if not matched_key:
            for label_lower, key in self._dir_key_by_label.items():
                if label_lower in lower:
                    matched_key = key
                    break

        if not matched_key:
            best_score = 0.0
            for key, label in DIRECTION_LABELS.items():
                score = SequenceMatcher(None, lower, label.lower()).ratio()
                if score > best_score:
                    best_score = score
                    matched_key = key
            if best_score < 0.4:
                matched_key = None

        if matched_key:
            direction_label = DIRECTION_LABELS.get(matched_key, matched_key)
            courses = self._directions.get(direction_label, [])
            if not courses:
                return ChatResponse(
                    reply=f"Hələlik {direction_label} istiqamətində kurs mövcud deyil.",
                    state="direction_selected",
                    actions=[
                        ActionButton(type="button", label="Proqramlaşdırma", value="proqramlashdirma"),
                        ActionButton(type="button", label="Kibertəhlükəsizlik", value="kiber"),
                        ActionButton(type="button", label="IT İnfrastruktur", value="infra"),
                    ],
                    courses=[],
                    capture="none",
                )

            session["data"]["direction"] = matched_key
            update_state(session, "course_selected")

            course_buttons = []
            for c in courses:
                course_buttons.append(
                    ActionButton(type="button", label=c["title"], value=c["id"])
                )
            course_buttons.append(ActionButton(type="button", label="← Geri", value="basha"))

            reply = (
                f"{direction_label} istiqamətində {len(courses)} kursumuz var.\n"
                "Hansı kurs haqqında ətraflı məlumat istəyirsən?"
            )

            return ChatResponse(
                reply=reply,
                state="course_selected",
                actions=course_buttons,
                courses=[],
                capture="none",
            )

        return self._try_answer_question(session, text)

    def _handle_course_action(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower().strip()

        if lower in ("basha", "basqa", "geri", "← geri", "←geri"):
            reset(session)
            return self._handle_start(session)

        selected_id = text.strip()
        course = None
        for c in self._courses:
            if c["id"] == selected_id or c["title"].lower() == lower:
                course = c
                break

        if not course:
            for c in self._courses:
                if selected_id in c["id"] or selected_id in c["title"].lower():
                    course = c
                    break

        if course:
            session["data"]["selected_course"] = course["id"]
            update_state(session, "faq")

            tools_str = ", ".join(course.get("tools", [])[:5])
            sched = course.get("schedule", {})
            sched_str = f"{sched.get('days', '')} · {sched.get('time', '')}" if sched else ""
            instructor = course.get("instructor", {})

            faq_buttons = []
            for i, faq in enumerate(course.get("faq", [])[:3]):
                faq_buttons.append(
                    ActionButton(type="button", label=faq["question"][:50], value=f"faq_{i}")
                )
            faq_buttons.append(ActionButton(type="button", label="Qeydiyyat", value="qeydiyyat"))
            faq_buttons.append(ActionButton(type="button", label="← Geri", value="basha"))

            reply = (
                f"📚 {course['title']}\n"
                f"💰 {course.get('priceAzn', '?')} AZN · {course.get('durationWeeks', '?')} həftə\n"
                f"📊 Səviyyə: {course.get('level', '?')} · Format: {course.get('format', '?')}\n"
                f"👨‍🏫 {instructor.get('name', '?')} — {instructor.get('title', '')}\n"
                f"📅 {sched_str}\n"
                f"🛠 Alətlər: {tools_str}\n\n"
                f"{course.get('shortDescription', '')}\n\n"
                "Aşağıdakı seçimlərdən birini et və ya sualını yaz:"
            )

            return ChatResponse(
                reply=reply,
                state="faq",
                actions=faq_buttons,
                courses=[self._course_to_card(course)],
                capture="none",
            )

        return ChatResponse(
            reply="Kurs seçmək üçün yuxarıdakı düymələrdən birinə bas.",
            state="course_selected",
            actions=[
                ActionButton(type="button", label="← Geri", value="basha"),
            ],
            courses=[],
            capture="none",
        )

    def _handle_faq(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower().strip()

        if lower in ("basha", "basqa", "geri", "← geri", "←geri"):
            reset(session)
            return self._handle_start(session)

        if lower in ("qeydiyyat", "qeydiyyatdan keç", "yazıl", "yazil"):
            return self._start_registration(session)

        course_id = session.get("data", {}).get("selected_course", "")
        course = None
        for c in self._courses:
            if c["id"] == course_id:
                course = c
                break

        if course and lower.startswith("faq_"):
            try:
                faq_idx = int(lower.replace("faq_", ""))
                faqs = course.get("faq", [])
                if 0 <= faq_idx < len(faqs):
                    answer = faqs[faq_idx]["answer"]
                    faq_buttons = []
                    for i, faq in enumerate(faqs[:3]):
                        faq_buttons.append(
                            ActionButton(type="button", label=faq["question"][:50], value=f"faq_{i}")
                        )
                    faq_buttons.append(ActionButton(type="button", label="Qeydiyyat", value="qeydiyyat"))
                    faq_buttons.append(ActionButton(type="button", label="← Geri", value="basha"))

                    return ChatResponse(
                        reply=answer,
                        state="faq",
                        actions=faq_buttons,
                        courses=[self._course_to_card(course)],
                        capture="none",
                    )
            except (ValueError, IndexError):
                pass

        faq_match = self._match_knowledge(text)
        if faq_match:
            return ChatResponse(
                reply=faq_match,
                state="faq",
                actions=[
                    ActionButton(type="button", label="Qeydiyyat", value="qeydiyyat"),
                    ActionButton(type="button", label="← Geri", value="basha"),
                ],
                courses=[self._course_to_card(course)] if course else [],
                capture="none",
            )

        return ChatResponse(
            reply=(
                "Bu suala cavab verməkdə çətinlik çəkirəm.\n\n"
                f"{CONTACT_INFO}\n\n"
                "Əlaqə səhifəmizdən də istifadə edə bilərsən: nexoracademy.az/elaqe"
            ),
            state="faq",
            actions=[
                ActionButton(type="button", label="Qeydiyyat", value="qeydiyyat"),
                ActionButton(type="button", label="← Geri", value="basha"),
            ],
            courses=[self._course_to_card(course)] if course else [],
            capture="none",
        )

    def _start_registration(self, session: dict) -> ChatResponse:
        update_state(session, "register_name")
        return ChatResponse(
            reply="Qeydiyyat üçün adını yaz.",
            state="register_name",
            actions=[],
            courses=[],
            capture="name",
        )

    def _handle_name(self, session: dict, text: str) -> ChatResponse:
        name = extract_name(text) or text.strip()
        if len(name) >= 2:
            session["data"]["name"] = name
            update_state(session, "register_phone")
            return ChatResponse(
                reply=f"Təşəkkür edirəm, {name}. İndi əlaqə üçün telefon nömrəni +994XXXXXXXXX formatında yaz.",
                state="register_phone",
                actions=[],
                courses=[],
                capture="phone",
            )
        return ChatResponse(
            reply="Zəhmət olmasa adını ən azı 2 simvolla yaz.",
            state="register_name",
            actions=[],
            courses=[],
            capture="name",
        )

    def _handle_phone(self, session: dict, text: str) -> ChatResponse:
        phone = extract_phone_clean(text)
        if phone:
            session["data"]["phone"] = phone
            update_state(session, "completed")
            self._store_lead(session)
            name = session["data"].get("name", "")
            return ChatResponse(
                reply=(
                    f"{name}, məlumatların qeydə alındı!\n\n"
                    f"Komandamız 1 iş günü ərzində {phone} nömrəsi ilə sənə zəng edəcək.\n\n"
                    "Başqa sualın varsa, mən hələ də buradayam!"
                ),
                state="completed",
                actions=[ActionButton(type="button", label="Yenidən başla", value="basha")],
                courses=[],
                capture="none",
            )
        return ChatResponse(
            reply="Telefon nömrəsini +994XXXXXXXXX formatında yaz.",
            state="register_phone",
            actions=[],
            courses=[],
            capture="phone",
        )

    def _handle_completed(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower().strip()
        if any(w in lower.split() for w in ["basha", "basqa", "geri", "yeniden", "yenidən", "başla"]):
            reset(session)
            return self._handle_start(session)
        return self._try_answer_question(session, text)

    def _try_answer_question(self, session: dict, text: str) -> ChatResponse:
        faq_match = self._match_knowledge(text)
        if faq_match:
            state = session.get("state", "start")
            return ChatResponse(
                reply=faq_match,
                state=state,
                actions=[
                    ActionButton(type="button", label="Proqramlaşdırma", value="proqramlashdirma"),
                    ActionButton(type="button", label="Kibertəhlükəsizlik", value="kiber"),
                    ActionButton(type="button", label="IT İnfrastruktur", value="infra"),
                ],
                courses=[],
                capture="none",
            )
        return self._handle_fallback(session)

    def _handle_fallback(self, session: dict) -> ChatResponse:
        state = session.get("state", "start")
        if state in ("start", "direction_selected"):
            update_state(session, "direction_selected")
            return ChatResponse(
                reply="Hansı istiqamət səni daha çox maraqlandırır?",
                state="direction_selected",
                actions=[
                    ActionButton(type="button", label="Proqramlaşdırma", value="proqramlashdirma"),
                    ActionButton(type="button", label="Kibertəhlükəsizlik", value="kiber"),
                    ActionButton(type="button", label="IT İnfrastruktur", value="infra"),
                ],
                courses=[],
                capture="none",
            )
        return ChatResponse(
            reply=(
                "Bu suala cavab verməkdə çətinlik çəkirəm.\n\n"
                f"{CONTACT_INFO}"
            ),
            state=state,
            actions=[ActionButton(type="button", label="← Geri", value="basha")],
            courses=[],
            capture="none",
        )

    def _match_knowledge(self, text: str) -> str | None:
        lower = text.lower().strip()
        best_entry = None
        best_score = 0.0

        for entry in self._knowledge:
            keywords = entry.get("keywords", [])
            for kw in keywords:
                if kw.lower() in lower:
                    return entry["text"]
                score = SequenceMatcher(None, kw.lower(), lower).ratio()
                if score > best_score:
                    best_score = score
                    best_entry = entry

        if best_score >= 0.55:
            return best_entry["text"]
        return None

    def _course_to_card(self, course: dict) -> CourseCard:
        return CourseCard(
            id=course["id"],
            name=course["title"],
            price=course.get("priceAzn"),
            category=course.get("direction", ""),
            level=course.get("level", ""),
            tools=course.get("tools", []),
            instructor=course.get("instructor", {}).get("name", ""),
            schedule=course.get("schedule", {}),
        )

    def _store_lead(self, session: dict):
        from core.lead_service import add_lead
        add_lead({
            "name": session["data"].get("name", ""),
            "phone": session["data"].get("phone", ""),
            "interest": session["data"].get("direction", ""),
            "level": "",
            "sessionId": session.get("sessionId", ""),
            "userId": session.get("userId", ""),
            "source": "chatbot",
        })

    def _finalize(self, session: dict, user_text: str, result: ChatResponse, state_before: str):
        add_history(session, "assistant", result.reply)
        session_save(session)
        telemetry.record(
            TurnMetrics(
                session_id=session["sessionId"],
                user_id=session.get("userId"),
                state_before=state_before,
                state_after=result.state,
                fallback_hit=False,
                user_message_len=len(user_text),
                reply_len=len(result.reply),
            )
        )
