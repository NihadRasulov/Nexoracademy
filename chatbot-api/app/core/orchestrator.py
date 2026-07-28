from __future__ import annotations

from core.session import get_or_create, update_state, add_history, reset
from core.intent import detect_intent, extract_phone as extract_phone_raw, extract_name
from core.extractor import extract_phone as extract_phone_clean
from rag.retriever import Retriever
from llm.client import LLMClient
from llm.prompts import SYSTEM_PROMPT
from data.loader import load_courses
from models.schemas import ChatResponse, ActionButton, CourseCard, LeadRequest

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
    "interest_selected": "İstifadəçi hələ maraq sahəsini seçməyib. Ona seçim etməyə kömək et: Proqramlaşdırma, Kibertəhlükəsizlik və ya Şəbəkə/DevOps. Əgər yazdığı bu sahələrdən birinə uyğun gəlmirsə, onu seçim etməyə yönləndir, amma sualına da cavab ver.",
    "level_selected": "İstifadəçi maraq sahəsini seçib, indi səviyyəsini seçməlidir: Başlanğıc, Orta və ya İrəli. Cavab verərkən onu səviyyə seçməyə yönləndir.",
    "recommendation": "İstifadəçiyə kurslar tövsiyə olunub. O, qeydiyyatdan keçmək, demo dərs istəmək və ya başqa sual verə bilər. Kömək et.",
    "lead_capture_name": "İstifadəçidən adını alırıq. Adını yazıbsa, təsdiq et və telefon nömrəsini soruş. Hələ yazmayıbsa, nəzakətlə adını soruş. Əgər sual verirsə, cavab ver, amma sonra yenə adını soruş.",
    "lead_capture_phone": "İstifadəçidən telefon nömrəsini alırıq (+994XXXXXXXXX). Nömrəni yazıbsa, təsdiq et. Yazmayıbsa, nömrəsini soruş. Sual verirsə, cavab ver, amma sonra yenə nömrəni soruş.",
    "completed": "İstifadəçi qeydiyyatdan keçib. Sərbəst sual verə bilər. Kömək et.",
}


class Orchestrator:
    def __init__(self, retriever: Retriever | None = None, llm: LLMClient | None = None):
        self.retriever = retriever
        self.llm = llm

    def process(self, message: str, session_id: str) -> ChatResponse:
        session = get_or_create(session_id)
        text = message.strip()

        print(f"USER: {text[:100]}")
        print(f"SESSION: {session_id} STATE: {session['state']}")

        if not text or text in ("/start", ""):
            return self._handle_start(session)

        if is_blocked(text):
            add_history(session, "user", text)
            return self._answer_with_llm(session, text)

        add_history(session, "user", text)
        state = session["state"]

        if state == "start":
            return self._handle_start(session, text)
        elif state == "interest_selected":
            return self._handle_interest(session, text)
        elif state == "level_selected":
            return self._handle_level(session, text)
        elif state == "recommendation":
            return self._handle_recommendation_reply(session, text)
        elif state == "lead_capture_name":
            return self._handle_name(session, text)
        elif state == "lead_capture_phone":
            return self._handle_phone(session, text)
        elif state == "completed":
            return self._handle_completed(session, text)
        else:
            return self._answer_with_llm(session, text)

    def _handle_start(self, session: dict, text: str = "") -> ChatResponse:
        if text:
            lower = text.lower().strip()
            greeting_words = ["salam", "hi", "hello", "hey", "merhaba", "sağol", "sagol"]
            is_just_greeting = lower in greeting_words
            if not is_just_greeting:
                update_state(session, "interest_selected")
                session["data"]["interest"] = "proqramlashdirma"
                return self._answer_with_llm(session, text)

        update_state(session, "interest_selected")
        return ChatResponse(
            reply="Salam əziz dostum! Nexora Academy-nin süni intellekt köməkçisiyəm. Çox şadam ki, bura gəlmisən! De görüm, səni ən çox hansı sahə maraqlandırır? Seçimini et, mən də sənə ən uyğun kursları tapaq!",
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
            return self._answer_with_llm(
                session, text,
                system_override=(
                    "İstifadəçi maraq sahəsini seçdi. Onu təbrik et və səviyyəsini soruş "
                    "(Başlanğıc, Orta, İrəli). Cavabında həm seçiminə uyğun rəy bildir, "
                    "həm də səviyyə seçimini etməsi üçün yönləndir."
                )
            )

        return self._answer_with_llm(session, text)

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
                    f"uyğun kurs hazırda mövcud deyil. Amma başqa sahə seçsən, bəlkə orada sənə uyğun bir şey tapaq!"
                )

            return ChatResponse(
                reply=reply,
                state="recommendation",
                actions=[
                    ActionButton(type="button", label="Qeydiyyatdan keç", value="qeydiyyat"),
                    ActionButton(type="button", label="Demo dərs istəyirəm", value="demo"),
                    ActionButton(type="button", label="Başqa sahə seç", value="basha"),
                ],
                courses=courses,
                capture="none",
            )

        return self._answer_with_llm(session, text)

    def _handle_recommendation_reply(self, session: dict, text: str) -> ChatResponse:
        lower = text.lower()
        if any(w in lower for w in ["basha", "basqa", "geri"]):
            reset(session)
            return self._handle_start(session)

        if any(w in lower.split() for w in ["kec", "keç", "yox", "istemirem", "istəmirəm"]):
            update_state(session, "completed")
            return ChatResponse(
                reply="Heç problem deyil, canın sağ olsun! Nə vaxt istəsən, yenə buyur. Başqa sualın varsa mən buradam. Yenidən başlamaq üçün 'başla' yazmağın kifayətdir.",
                state="completed",
                actions=[],
                courses=[],
                capture="none",
            )

        intent = detect_intent(text)
        if intent in ("lead_phone", "lead_name", "registration"):
            update_state(session, "lead_capture_name")
            return self._answer_with_llm(session, text)

        return self._answer_with_llm(session, text)

    def _handle_name(self, session: dict, text: str) -> ChatResponse:
        name = extract_name(text) or text.strip()
        if len(name) >= 2:
            session["data"]["name"] = name
            update_state(session, "lead_capture_phone")
            return self._answer_with_llm(session, text)

        intent = detect_intent(text)
        if intent == "question":
            return self._answer_with_llm(session, text)

        return self._answer_with_llm(session, text)

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

        return self._answer_with_llm(session, text)

    def _handle_completed(self, session: dict, text: str = "") -> ChatResponse:
        lower = text.lower().strip()
        if any(w in lower.split() for w in ["basha", "basqa", "geri", "yeniden", "yenidən"]):
            reset(session)
            return self._handle_start(session)
        return self._answer_with_llm(session, text)

    def _answer_with_llm(self, session: dict, text: str, system_override: str | None = None) -> ChatResponse:
        from data.loader import load_courses as _load_courses, load_knowledge

        context_parts = []

        courses = _load_courses()
        if courses:
            context_parts.append("Mövcud kurslar:\n" + "\n".join(
                f"- {c.get('title','')} ({c.get('direction','')}, {c.get('level','')}) - {c.get('priceAzn','')} AZN"
                for c in courses[:10]
            ))

        for entry in load_knowledge():
            context_parts.append(f"[{entry['id']}] {entry['text']}")

        if self.retriever:
            try:
                results = self.retriever.retrieve(text, top_k=3)
                print(f"DOCS: {len(results)} retrieved")
                if results:
                    rag_text = "RAG məlumatları:\n" + "\n\n".join(r["text"] for r in results)
                    context_parts.insert(0, rag_text)
            except Exception as e:
                print(f"[RAG ERROR] {e}")

        context = "\n\n".join(context_parts) if context_parts else ""

        if system_override:
            system_msg = system_override
        else:
            state_hint = STATE_CONTEXT.get(session["state"], "")
            system_msg = SYSTEM_PROMPT
            if state_hint:
                system_msg += f"\n\nCari vəziyyət: {state_hint}"

        messages = [
            {"role": "system", "content": system_msg},
        ]
        if context:
            messages.append({"role": "system", "content": f"Kontekst məlumatı:\n{context}"})

        for entry in session["history"][-6:]:
            messages.append({"role": entry["role"], "content": entry["text"]})

        messages.append({"role": "user", "content": text})

        if not self.llm:
            return ChatResponse(
                reply="Hal-hazırda texniki problem var. Zəhmət olmasa sonra yenidən yoxlayın.",
                state=session["state"],
                actions=[],
                courses=[],
                capture="none",
            )

        try:
            reply = self.llm.chat(messages)
        except Exception as e:
            print(f"LLM ERROR: {e}")
            reply = "Hal-hazırda texniki problem var. Zəhmət olmasa sonra yenidən yoxlayın."

        return ChatResponse(
            reply=reply,
            state=session["state"],
            actions=[],
            courses=[],
            capture="none",
        )

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

    def _store_lead(self, session: dict):
        from .lead_service import add_lead
        add_lead({
            "name": session["data"].get("name", ""),
            "phone": session["data"].get("phone", ""),
            "interest": session["data"].get("interest", ""),
            "level": session["data"].get("level", ""),
            "sessionId": id(session),
        })


def is_blocked(text: str) -> bool:
    import re
    patterns = [
        r"ignore (all|previous|the) (instructions|rules)",
        r"system prompt",
        r"you are now",
        r"act as",
        r"jailbreak",
        r"təlimatları unut",
        r"qaydaları unut",
        r"rolunu dəyiş",
        r"sən indi",
    ]
    lower = text.lower()
    for p in patterns:
        if re.search(p, lower):
            return True
    return False
