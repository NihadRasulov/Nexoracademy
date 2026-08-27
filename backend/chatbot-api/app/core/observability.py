"""
Observability layer – structured logging + Prometheus metrics.
"""
from __future__ import annotations

import json
import logging
import time
from dataclasses import asdict, dataclass, field
from typing import Optional

from core.config import settings

logger = logging.getLogger("nexora.observability")


@dataclass
class TurnMetrics:
    session_id: str
    user_id: Optional[str]
    state_before: str
    state_after: str
    fallback_hit: bool
    user_message_len: int
    reply_len: int
    timestamp: float = field(default_factory=time.time)


_prom_available = False

if settings.metrics_enabled:
    try:
        from prometheus_client import Counter, Histogram

        _chat_turns = Counter("nexora_chat_turns_total", "Total chat turns processed")
        _fallbacks = Counter("nexora_fallback_hits_total", "Total fallback responses served")
        _leads = Counter("nexora_leads_total", "Total leads captured", ["interest"])
        _state_transitions = Counter(
            "nexora_state_transitions_total",
            "State machine transitions",
            ["from_state", "to_state"],
        )
        _prom_available = True
        logger.info("prometheus_metrics_enabled")
    except Exception as exc:
        logger.warning("prometheus_init_failed error=%s – metrics disabled", exc)


class ObservabilityMiddleware:
    def record(self, metrics: TurnMetrics) -> None:
        self._log(metrics)
        if _prom_available:
            self._increment(metrics)

    def record_lead(self, interest: str) -> None:
        if _prom_available:
            try:
                _leads.labels(interest=interest or "unknown").inc()
            except Exception:
                pass


    def _log(self, m: TurnMetrics) -> None:
        record = {
            "event": "chat_turn",
            **asdict(m),
        }
        logger.info(json.dumps(record, ensure_ascii=False))

    def _increment(self, m: TurnMetrics) -> None:
        try:
            _chat_turns.inc()
            if m.fallback_hit:
                _fallbacks.inc()
            if m.state_before != m.state_after:
                _state_transitions.labels(
                    from_state=m.state_before,
                    to_state=m.state_after,
                ).inc()
        except Exception as exc:
            logger.debug("metrics_increment_error error=%s", exc)


telemetry = ObservabilityMiddleware()
