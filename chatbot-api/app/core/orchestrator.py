"""
Conversation orchestrator – production-grade rewrite.
"""
from __future__ import annotations

import logging
import time
from typing import TYPE_CHECKING

from core.session import (
    get_or_create,
    save as session_save,
    update_state,
    add_history,
    reset,
    get_context_history,
)
from core.intent import detect_intent, extract_phone as extract_phone_raw, extract_name, is_blocked
from core.extractor import extract_phone as extract_phone_clean
from core.personalisation import PersonalisationInjector
from core.parser import ResponseParser
from core.observability import telemetry, TurnMetrics

if TYPE_CHECKING:
    from rag.retriever import Retriever
    from llm.client import LLMClient

from llm.prompts import SYSTEM_PROMPT
from data.loader import load_courses
from models.schemas import ChatResponse, ActionButton, CourseCard, LeadRequest
from core.config import settings

logger = logging.getLogger("nexora.orchestrator")

INTEREST_ACTIONS = [
    ActionButton(type="button", label="Proqramlaşdırma", value="proqramlashdirma"),
    ActionButton(type="button", label="Kibertəhlükəsizlik", value="kiber"),
    ActionButton(type="button", label="Şəbəkə / DevOps", value="shebeke"),
]

LEVEL_ACTIONS = [
    ActionButton(type="button", label="Yeni başlayan", value="baslangic"),
    ActionButton(type="button", label="Orta səviyyə", value="orta"),
    ActionButton(type="button", label="Təcrübəli", value="ireli"),
]

RECOMMENDATION_ACTIONS = [
    ActionButton(type="button", label="Qeydiyyatdan keç", value="qeydiyyat"),
    ActionButton(type="button", label="Demo dərs istəyirəm", value="demo"),
    ActionButton(type="button", label="Başqa sahə seç", value="basha"),
]

DIRECTION_MAP = {
    "proqramlashdirma": "Proqramlaşdırma",
    "kiber": "Kibertəhlükəsizlik",
    "shebeke": "IT İnfrastruktur",
}

LEVEL_MAP = {
    "baslangic": "Başlanğıc",
    "orta": "Orta",
    "ireli": "İrəli",
}

STATE_CONTEXT = {
    "interest_selected": (
        "İstifadəçi hələ maraq sahəsini seçməyib. Ona seçim etməyə kömək et: "
        "Proqramlaşdırma, Kibertəhlükəsizlik və ya Şəbəkə/DevOps. "
        "Əgər yazdığı bu sahələrdən birinə uyğun gəlmirsə, onu seçim etməyə yönləndir, amma sualına da cavab ver."
    ),
    "level_selected": (
        "İstifadəçi maraq sahəsini seçib, indi səviyyəsini seçməlidir: "
        "Başlanğıc, Orta və ya İrəli. Cavab verərkən onu səviyyə seçməyə yönləndir."
    ),
    "recommendation": (
        "İstifadəçiyə kurslar tövsiyə olunub. O, qeydiyyatdan keçmək, "
        "demo dərs istəmək və ya başqa sual verə bilər. Kömək et."
    ),
    "lead_capture_name": (
        "İstifadəçidən adını alırıq. Adını yazıbsa, təsdiq et və telefon nömrəsini soruş. "
        "Hələ yazmayıbsa, nəzakətlə adını soruş. "
        "Əgər sual verirsə, cavab ver, amma sonra yenə adını soruş."
    ),
    "lead_capture_phone": (
        "İstifadəçidən telefon nömrəsini alırıq (+994XXXXXXXXX). "
        "Nömrəni yazıbsa, təsdiq et. Yazmayıbsa, nömrəsini soruş. "
        "Sual verirsə, cavab ver, amma sonra yenə nömrəni soruş."
    ),
    "completed": "İstifadəçi qeydiyyatdan keçib. Sərbəst sual verə bilər. Kömək et.",
}


class Orchestrator:
    def __init__(
        self,
        retriever: "Retriever | None" = None,
        llm: "LLMClient | None" = None,
    ):
        self.retriever = retriever
        self.llm = llm
        self._personaliser = PersonalisationInjector()
        self._parser = ResponseParser()

    def process(self, message: str, session_id: str, user_id: str | None = None) -> ChatResponse:
        session = get_or_create(session_id, user_id)
        text = message.strip()[:2000]

        state_before = session["state"]

        logger.debug("turn_start session=%s state=%s user=%s", session_id, state_before, user_id)

        if is_blocked(text):
            add_history(session, "user", text)
            result = self._answer_with_llm(session, text)
            self._finalize(session, text, result, state_before, llm_called=True)
            return result

        if not text or text in ("/start", ""):
            result = self._handle_start(session)
            self._finalize(session, text, result, state_before, llm_called=False)
            return result

        add_history(session, "user", text)
        state = session["state"]

        llm_called = False
        if state == "start":
            result = self._handle_start(session, text)
        elif state == "interest_selected":
            result = self._handle_interest(session, text)
        elif state == "level_selected":
            result = self._handle_level(session, text)
        elif state == "recommendation":
            result = self._handle_recommendation_reply(session, text)
        elif state == "lead_capture_name":
            result = self._handle_name(session, text)
        elif state == "lead_capture_phone":
            result = self._handle_phone(session, text)
        elif state == "completed":
            result = self._handle_completed(session, text)
            llm_called = True
        else:
            result = self._answer_with_llm(session, text)
            llm_called = True

        if not llm_called:
            llm_called = getattr(result, "_llm_called", False)

        self._finalize(session, text, result, state_before, llm_called=llm_called)
        return result

    def _handle_start(self, session: dict, text: str = "") -> ChatResponse:
        if text:
            lower = text.lower().strip()
            greeting_words = ["salam", "hi", "hello", "hey", "merhaba", "sağol", "sagol"]
            if lower not in greeting_words:
                update_state(session, "interest_selected")
                return self._answer_with_llm(session, text)

        update_state(session, "interest_selected")

        name = session.get("data", {}).get("name")
        greeting = f"Xoş gördük, {name}!" if name else "Salam əziz dostum!"

        return ChatResponse(
            reply=(
                f"{greeting} Nexora Academy-nin süni intellekt köməkçisiyəm. "
                "Çox şadam ki, bura gəlmisən! De görüm, səni ən çox hansı sahə maraqlandırır? "
                "Seçimini et, mən də sənə ən uyğun kursları tapaq!"
            ),
            state="interest_selected",
            actions=INTEREST_ACTIONS,
            courses=[],
            capture="none",
        )

    def _handle_interest(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower().strip()
        matched = None
        for key, label in DIRECTION_MAP.items():
            if key in lower or label.lower() in lower:
                matched = key
                break

        if matched:
            session["data"]["interest"] = matched
            update_state(session, "level_selected")
            direction_label = DIRECTION_MAP.get(matched, "seçdiyin sahə")
            return ChatResponse(
                reply=(
                    f"Əla seçimdir! {direction_label} istiqamətində sənə uyğun proqramı "
                    "tapmaq üçün hazırkı səviyyəni seç: yeni başlayan, orta səviyyə və ya təcrübəli."
                ),
                state="level_selected",
                actions=LEVEL_ACTIONS,
                courses=[],
                capture="none",
            )

        result = self._answer_with_llm(session, text)
        result._llm_called = True
        return result

    def _handle_level(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower().strip()
        matched = None
        for key, label in LEVEL_MAP.items():
            if key in lower or label.lower() in lower:
                matched = key
                break

        if matched:
            session["data"]["level"] = matched
            update_state(session, "recommendation")
            courses = self._recommend_courses(session["data"]["interest"], matched)

            direction_label = DIRECTION_MAP.get(session["data"]["interest"], "")
            level_label = LEVEL_MAP.get(matched, "")

            if courses:
                reply = (
                    f"Super! Sənin üçün {direction_label} sahəsində {level_label.lower()} "
                    f"səviyyəsinə uyğun ən yaxşı kursları seçdim. Aşağıda kursları görə bilərsən.\n\n"
                    f"Bunlardan hansısa xoşuna gəldi? Demo dərsdə iştirak etmək və ya "
                    f"qeydiyyatdan keçmək üçün adını yaz, səninlə əlaqə saxlayaq!"
                )
            else:
                reply = (
                    f"Təəssüf ki, {direction_label} sahəsində {level_label.lower()} səviyyəyə "
                    f"uyğun kurs hazırda mövcud deyil. "
                    f"Amma başqa sahə seçsən, bəlkə orada sənə uyğun bir şey tapaq!"
                )

            return ChatResponse(
                reply=reply,
                state="recommendation",
                actions=RECOMMENDATION_ACTIONS,
                courses=courses,
                capture="none",
            )

        result = self._answer_with_llm(session, text)
        result._llm_called = True
        return result

    def _handle_recommendation_reply(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower()

        if any(w in lower for w in ["basha", "basqa", "geri"]):
            reset(session)
            return self._handle_start(session)

        if any(w in lower.split() for w in ["kec", "keç", "yox", "istemirem", "istəmirəm"]):
            update_state(session, "completed")
            return ChatResponse(
                reply=(
                    "Heç problem deyil, canın sağ olsun! Nə vaxt istəsən, yenə buyur. "
                    "Başqa sualın varsa mən buradam. Yenidən başlamaq üçün 'başla' yazmağın kifayətdir."
                ),
                state="completed",
                actions=[ActionButton(type="button", label="Yenidən başla", value="basha")],
                courses=[],
                capture="none",
            )

        intent = detect_intent(text)
        if intent in ("lead_phone", "lead_name", "registration") or any(
            word in lower for word in ["qeydiyyat", "demo"]
        ):
            update_state(session, "lead_capture_name")
            return ChatResponse(
                reply="Məmnuniyyətlə kömək edərəm. Əvvəlcə adını yaz.",
                state="lead_capture_name",
                actions=[],
                courses=[],
                capture="name",
            )

        result = self._answer_with_llm(session, text)
        result._llm_called = True
        return result

    def _handle_name(self, session: dict, text: str) -> ChatResponse:
        name = extract_name(text) or text.strip()
        if len(name) >= 2:
            session["data"]["name"] = name
            update_state(session, "lead_capture_phone")
            return ChatResponse(
                reply=(
                    f"Təşəkkür edirəm, {name}. İndi əlaqə üçün telefon nömrəni "
                    "+994XXXXXXXXX formatında yaz."
                ),
                state="lead_capture_phone",
                actions=[],
                courses=[],
                capture="phone",
            )

        return ChatResponse(
            reply="Zəhmət olmasa adını ən azı 2 simvolla yaz.",
            state="lead_capture_name",
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
                    f"{name}, məlumatların qeydə alındı! Çox sevindim səni tanıdığıma.\n\n"
                    f"Komandamız 1 iş günü ərzində {phone} nömrəsi ilə sənə zəng edəcək. "
                    f"Sənə uyğun kurs haqqında ətraflı məlumat verəcəklər.\n\n"
                    f"Başqa sualın varsa, mən hələ də buradayam! İstənilən vaxt yaz."
                ),
                state="completed",
                actions=[ActionButton(type="button", label="Yenidən başla", value="basha")],
                courses=[],
                capture="none",
            )

        return ChatResponse(
            reply="Telefon nömrəsini +994XXXXXXXXX formatında yaz.",
            state="lead_capture_phone",
            actions=[],
            courses=[],
            capture="phone",
        )

    def _handle_completed(self, session: dict, text: str = "") -> ChatResponse:
        lower = text.lower().strip()
        if any(w in lower.split() for w in ["basha", "basqa", "geri", "yeniden", "yenidən"]):
            reset(session)
            return self._handle_start(session)
        return self._answer_with_llm(session, text)

    def _answer_with_llm(self, session: dict, text: str) -> ChatResponse:
        from data.loader import load_knowledge

        context_parts: list[str] = []

        if self.retriever:
            try:
                results = self.retriever.retrieve(text, top_k=settings.rag_top_k)
                if results:
                    rag_text = "RAG məlumatları:\n" + "\n\n".join(r["text"] for r in results)
                    context_parts.append(rag_text)
                    logger.debug("rag_retrieved count=%d", len(results))
            except Exception as exc:
                logger.warning("rag_error error=%s", exc)

        courses = load_courses()
        if courses:
            context_parts.append(
                "Mövcud kurslar:\n" + "\n".join(
                    f"- {c.get('title','')} ({c.get('direction','')}, {c.get('level','')}) "
                    f"- {c.get('priceAzn','')} AZN"
                    for c in courses[:settings.context_max_courses]
                )
            )

        for entry in load_knowledge():
            context_parts.append(f"[{entry['id']}] {entry['text']}")

        context = "\n\n".join(context_parts)

        state_hint = STATE_CONTEXT.get(session["state"], "")
        system_content = SYSTEM_PROMPT
        if state_hint:
            system_content += f"\n\nCari vəziyyət: {state_hint}"

        messages: list[dict] = [{"role": "system", "content": system_content}]

        if context:
            messages.append({"role": "system", "content": f"Kontekst məlumatı:\n{context}"})

        personalisation = self._personaliser.build(session)
        if personalisation:
            messages.append({"role": "system", "content": personalisation})

        messages.extend(get_context_history(session))

        messages.append({"role": "user", "content": text})

        if not self.llm:
            return self._fallback_response(session)

        t0 = time.time()
        try:
            from llm.client import LLMCircuitOpenError
            raw_reply = self.llm.chat(messages)
            latency_ms = int((time.time() - t0) * 1000)
            logger.debug("llm_reply latency_ms=%d", latency_ms)
        except LLMCircuitOpenError:
            logger.warning("llm_circuit_open – returning fallback")
            telemetry.record_llm_error("circuit_open")
            return self._fallback_response(session)
        except Exception as exc:
            latency_ms = int((time.time() - t0) * 1000)
            logger.error("llm_error error=%s latency_ms=%d", exc, latency_ms)
            telemetry.record_llm_error(type(exc).__name__)
            return self._fallback_response(session)

        parsed = self._parser.parse(raw_reply, session["state"])

        return ChatResponse(
            reply=parsed["reply"],
            state=parsed.get("state", session["state"]),
            actions=parsed.get("actions", []),
            courses=parsed.get("courses", []),
            capture=parsed.get("capture", "none"),
        )

    def _fallback_response(self, session: dict) -> ChatResponse:
        state = session.get("state", "start")
        if state in ("start", "interest_selected"):
            return ChatResponse(
                reply="Sənə uyğun kurs tapmaq üçün maraqlandığın istiqaməti seç.",
                state="interest_selected",
                actions=INTEREST_ACTIONS,
                courses=[],
                capture="none",
            )
        if state == "level_selected":
            return ChatResponse(
                reply="Hazırkı səviyyəni seç: yeni başlayan, orta səviyyə və ya təcrübəli.",
                state=state,
                actions=LEVEL_ACTIONS,
                courses=[],
                capture="none",
            )
        if state == "recommendation":
            return ChatResponse(
                reply="Kurslardan biri ilə bağlı demo dərs və ya qeydiyyat üçün seçim et.",
                state=state,
                actions=RECOMMENDATION_ACTIONS,
                courses=[],
                capture="none",
            )
        if state == "lead_capture_name":
            return ChatResponse(
                reply="Qeydiyyatı davam etdirmək üçün adını yaz.",
                state=state,
                actions=[],
                courses=[],
                capture="name",
            )
        if state == "lead_capture_phone":
            return ChatResponse(
                reply="Əlaqə üçün telefon nömrəni +994XXXXXXXXX formatında yaz.",
                state=state,
                actions=[],
                courses=[],
                capture="phone",
            )
        return ChatResponse(
            reply="Yeni kurs seçimi üçün aşağıdakı düymədən istifadə et.",
            state="completed",
            actions=[ActionButton(type="button", label="Yenidən başla", value="basha")],
            courses=[],
            capture="none",
        )

    def _finalize(
        self,
        session: dict,
        user_text: str,
        result: ChatResponse,
        state_before: str,
        llm_called: bool,
    ):
        add_history(session, "assistant", result.reply)
        session_save(session)

        telemetry.record(
            TurnMetrics(
                session_id=session["sessionId"],
                user_id=session.get("userId"),
                state_before=state_before,
                state_after=result.state,
                llm_called=llm_called,
                llm_latency_ms=None,
                fallback_hit=False,
                retry_count=0,
                user_message_len=len(user_text),
                reply_len=len(result.reply),
            )
        )

    def _store_lead(self, session: dict):
        from core.lead_service import add_lead
        interest = session["data"].get("interest", "")
        add_lead({
            "name": session["data"].get("name", ""),
            "phone": session["data"].get("phone", ""),
            "interest": interest,
            "level": session["data"].get("level", ""),
            "sessionId": session.get("sessionId", ""),
            "userId": session.get("userId", ""),
            "source": "chatbot",
        })
        telemetry.record_lead(interest)

    def _recommend_courses(self, interest: str, level: str) -> list[CourseCard]:
        courses = load_courses()
        direction_label = DIRECTION_MAP.get(interest)
        level_label = LEVEL_MAP.get(level)

        matched = courses
        if direction_label:
            by_dir = [c for c in courses if c.get("direction") == direction_label]
            if by_dir:
                matched = by_dir
        if level_label:
            by_lvl = [c for c in matched if c.get("level") == level_label]
            if by_lvl:
                matched = by_lvl

        return [
            CourseCard(
                id=c["id"],
                name=c["title"],
                price=c.get("priceAzn"),
                category=c.get("direction", ""),
                level=c.get("level", ""),
                tools=c.get("tools", []),
                instructor=c.get("instructor", {}).get("name", ""),
                schedule=c.get("schedule", {}),
            )
            for c in matched[:4]
        ]
