"""
Content-type agnostic response parser.

Handles three shapes of LLM output:
  1. Clean JSON  {"reply": "...", "capture": "none", ...}
  2. JSON inside a markdown code fence ```json {...} ```
  3. Plain text  (re-derives actions/capture from current conversation state)

Returns a normalised dict that maps directly to ChatResponse fields.
"""
from __future__ import annotations

import json
import logging
import re

logger = logging.getLogger("nexora.parser")


def _interest_actions():
    from core.orchestrator import INTEREST_ACTIONS
    return INTEREST_ACTIONS


def _level_actions():
    from core.orchestrator import LEVEL_ACTIONS
    return LEVEL_ACTIONS


def _recommendation_actions():
    from core.orchestrator import RECOMMENDATION_ACTIONS
    return RECOMMENDATION_ACTIONS


def _restart_actions():
    from models.schemas import ActionButton
    return [ActionButton(type="button", label="Yenidən başla", value="basha")]


_CAPTURE_MAP: dict[str, str] = {
    "lead_capture_name": "name",
    "lead_capture_phone": "phone",
}


def _actions_for_state(state: str) -> list:
    try:
        if state == "interest_selected":
            return _interest_actions()
        if state == "level_selected":
            return _level_actions()
        if state == "recommendation":
            return _recommendation_actions()
        if state == "completed":
            return _restart_actions()
    except Exception:
        pass
    return []


class ResponseParser:
    def parse(self, raw: str, current_state: str) -> dict:
        raw = (raw or "").strip()

        result = self._try_json(raw, current_state)
        if result:
            return result

        fence = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", raw, re.DOTALL)
        if fence:
            result = self._try_json(fence.group(1), current_state)
            if result:
                return result

        inline = re.search(r"\{[^{}]*\"reply\"[^{}]*\}", raw, re.DOTALL)
        if inline:
            result = self._try_json(inline.group(0), current_state)
            if result:
                return result

        clean_text = self._strip_markdown(raw)
        logger.debug("parser_fallback state=%s text_len=%d", current_state, len(clean_text))
        return self._from_plain_text(clean_text, current_state)

    def _try_json(self, text: str, current_state: str) -> dict | None:
        try:
            obj = json.loads(text)
        except (json.JSONDecodeError, ValueError):
            return None

        if not isinstance(obj, dict) or "reply" not in obj:
            return None

        return self._normalise(obj, current_state)

    def _normalise(self, obj: dict, current_state: str) -> dict:
        state = obj.get("state", current_state)

        actions = obj.get("actions")
        if not actions:
            actions = _actions_for_state(state)

        capture = obj.get("capture", _CAPTURE_MAP.get(state, "none"))

        return {
            "reply": str(obj["reply"]).strip(),
            "state": state,
            "actions": actions,
            "courses": obj.get("courses", []),
            "capture": capture,
        }

    def _from_plain_text(self, text: str, current_state: str) -> dict:
        return {
            "reply": text or "Bir az gözlə, hər şeyi düzəltməyə çalışıram.",
            "state": current_state,
            "actions": _actions_for_state(current_state),
            "courses": [],
            "capture": _CAPTURE_MAP.get(current_state, "none"),
        }

    def _strip_markdown(self, text: str) -> str:
        text = re.sub(r"```[\w]*\n?", "", text)
        text = re.sub(r"```", "", text)
        text = re.sub(r"\*{1,3}(.*?)\*{1,3}", r"\1", text)
        return text.strip()
