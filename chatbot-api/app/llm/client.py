"""
Production-grade LLM client for OpenRouter.
"""
from __future__ import annotations

import json
import logging
import time
from typing import Generator

import httpx
from tenacity import (
    retry,
    retry_if_exception,
    stop_after_attempt,
    wait_exponential,
    before_sleep_log,
)

from core.config import settings

logger = logging.getLogger("nexora.llm")


class LLMError(Exception):
    pass


class LLMParseError(LLMError):
    pass


class LLMCircuitOpenError(LLMError):
    pass


class LLMQuotaError(LLMError):
    pass


def _is_retryable(exc: BaseException) -> bool:
    if isinstance(exc, (httpx.TimeoutException, httpx.NetworkError, httpx.RemoteProtocolError)):
        return True
    if isinstance(exc, httpx.HTTPStatusError):
        return exc.response.status_code in (429, 500, 502, 503, 504)
    if isinstance(exc, LLMParseError):
        return True
    return False


class CircuitBreaker:
    def __init__(self, redis_client=None):
        self.redis = redis_client
        self._mem_failures = 0
        self._mem_last_fail = 0.0

    def _key_failures(self) -> str:
        return "cb:llm:failures"

    def _key_last_fail(self) -> str:
        return "cb:llm:last_fail"

    def state(self) -> str:
        threshold = settings.cb_failure_threshold
        window = settings.cb_recovery_window_s

        try:
            if self.redis:
                failures = int(self.redis.get(self._key_failures()) or 0)
                last_fail = float(self.redis.get(self._key_last_fail()) or 0)
            else:
                failures = self._mem_failures
                last_fail = self._mem_last_fail
        except Exception:
            failures = self._mem_failures
            last_fail = self._mem_last_fail

        if failures >= threshold:
            if time.time() - last_fail < window:
                return "open"
            return "half-open"
        return "closed"

    def record_failure(self):
        now = time.time()
        try:
            if self.redis:
                pipe = self.redis.pipeline()
                pipe.incr(self._key_failures())
                pipe.set(self._key_last_fail(), now)
                pipe.expire(self._key_failures(), settings.cb_recovery_window_s * 10)
                pipe.execute()
                return
        except Exception:
            pass
        self._mem_failures += 1
        self._mem_last_fail = now

    def record_success(self):
        try:
            if self.redis:
                self.redis.delete(self._key_failures(), self._key_last_fail())
                return
        except Exception:
            pass
        self._mem_failures = 0
        self._mem_last_fail = 0.0


class LLMClient:
    def __init__(
        self,
        api_key: str | None = None,
        model: str | None = None,
        redis_client=None,
    ):
        self.api_key = api_key or settings.openrouter_api_key
        self.model_name = model or settings.openrouter_model
        if not self.api_key:
            raise ValueError("OPENROUTER_API_KEY not set.")
        self.api_url = settings.openrouter_url
        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://nexora.az",
        }
        self.cb = CircuitBreaker(redis_client)

    def chat(self, messages: list[dict], stream: bool = False) -> str:
        cb_state = self.cb.state()
        if cb_state == "open":
            logger.warning("circuit_breaker=open – returning fallback")
            raise LLMCircuitOpenError("Circuit breaker is open")

        logger.debug("llm_call model=%s cb=%s", self.model_name, cb_state)

        if stream:
            return self._call_once(messages)

        try:
            result = self._call_with_retry(messages)
            self.cb.record_success()
            return result
        except LLMQuotaError:
            self.cb.record_failure()
            raise
        except Exception:
            self.cb.record_failure()
            raise

    def chat_stream(self, messages: list[dict]) -> Generator[str, None, None]:
        cb_state = self.cb.state()
        if cb_state == "open":
            yield "Hal-hazırda texniki problem var. Zəhmət olmasa sonra yenidən yoxlayın."
            return

        payload = self._build_payload(messages, stream=True)
        try:
            with httpx.Client(timeout=settings.llm_timeout_s) as client:
                resp = client.post(self.api_url, headers=self.headers, json=payload)
                resp.raise_for_status()
                for line in resp.iter_lines():
                    if line.startswith("data: "):
                        chunk = line[6:]
                        if chunk == "[DONE]":
                            break
                        try:
                            data = json.loads(chunk)
                            content = data["choices"][0]["delta"].get("content", "")
                            if content:
                                yield content
                        except (json.JSONDecodeError, KeyError, IndexError):
                            pass
            self.cb.record_success()
        except Exception as exc:
            self.cb.record_failure()
            logger.error("llm_stream_error error=%s", exc)
            yield "Hal-hazırda texniki problem var. Zəhmət olmasa sonra yenidən yoxlayın."

    def generate(self, prompt: str, system_prompt: str | None = None) -> str:
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})
        return self.chat(messages)

    def generate_stream(self, prompt: str, system_prompt: str | None = None) -> Generator[str, None, None]:
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})
        yield from self.chat_stream(messages)

    def answer_question(self, question: str, context: str) -> str:
        from llm.prompts import SYSTEM_PROMPT
        messages = [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "system", "content": f"Kontekst məlumatı:\n{context}"},
            {"role": "user", "content": question},
        ]
        return self.chat(messages)

    def _build_payload(self, messages: list[dict], stream: bool = False) -> dict:
        return {
            "model": self.model_name,
            "messages": messages,
            "temperature": settings.llm_temperature,
            "max_tokens": settings.llm_max_tokens,
            "stream": stream,
        }

    @retry(
        retry=retry_if_exception(_is_retryable),
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=1, max=8),
        before_sleep=before_sleep_log(logger, logging.WARNING),
        reraise=True,
    )
    def _call_with_retry(self, messages: list[dict]) -> str:
        return self._call_once(messages)

    def _call_once(self, messages: list[dict]) -> str:
        payload = self._build_payload(messages)
        t0 = time.time()
        with httpx.Client(timeout=settings.llm_timeout_s) as client:
            resp = client.post(self.api_url, headers=self.headers, json=payload)

        latency_ms = int((time.time() - t0) * 1000)

        if resp.status_code in (401, 402, 403):
            logger.error("llm_quota_error status=%d", resp.status_code)
            raise LLMQuotaError(f"LLM returned {resp.status_code}")

        resp.raise_for_status()

        try:
            data = resp.json()
        except Exception as exc:
            raise LLMParseError(f"Non-JSON response body: {exc}") from exc

        if "error" in data and "choices" not in data:
            raise LLMParseError(f"LLM error envelope: {data['error']}")

        choices = data.get("choices")
        if not choices or not isinstance(choices, list):
            raise LLMParseError(f"Missing 'choices' in response: {list(data.keys())}")

        content = choices[0].get("message", {}).get("content")
        if content is None:
            raise LLMParseError("Null content in LLM response (content_filter?)")

        logger.info(
            "llm_success latency_ms=%d tokens=%s",
            latency_ms,
            data.get("usage", {}).get("total_tokens", "?"),
        )
        return content
