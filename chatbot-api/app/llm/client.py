import os
import json
import httpx
from typing import Generator

from .prompts import SYSTEM_PROMPT


class LLMClient:
    def __init__(self, api_key: str | None = None, model: str | None = None):
        self.api_key = api_key or os.environ.get("OPENROUTER_API_KEY")
        self.model_name = model or os.environ.get("OPENROUTER_MODEL", "openai/gpt-4o-mini")
        if not self.api_key:
            raise ValueError("OPENROUTER_API_KEY not set. Pass api_key or set OPENROUTER_API_KEY env var.")
        self.api_url = "https://openrouter.ai/api/v1/chat/completions"
        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://nexora.az",
        }

    def chat(self, messages: list[dict], stream: bool = False) -> str:
        print("LLM CALLED")
        payload = {
            "model": self.model_name,
            "messages": messages,
            "temperature": 0.7,
            "max_tokens": 600,
            "stream": stream,
        }
        with httpx.Client(timeout=60) as client:
            resp = client.post(self.api_url, headers=self.headers, json=payload)
            resp.raise_for_status()
            if stream:
                return self._handle_stream(resp)
            data = resp.json()
        content = data["choices"][0]["message"].get("content")
        if content is None:
            content = "Hal-hazırda texniki problem var. Zəhmət olmasa sonra yenidən yoxlayın."
        return content

    def _handle_stream(self, resp) -> str:
        full = ""
        for line in resp.iter_lines():
            if line.startswith("data: "):
                chunk = line[6:]
                if chunk == "[DONE]":
                    break
                try:
                    data = json.loads(chunk)
                    content = data["choices"][0]["delta"].get("content", "")
                    if content:
                        full += content
                except json.JSONDecodeError:
                    pass
        return full or "Hal-hazırda texniki problem var. Zəhmət olmasa sonra yenidən yoxlayın."

    def chat_stream(self, messages: list[dict]) -> Generator[str, None, None]:
        print("LLM CALLED")
        payload = {
            "model": self.model_name,
            "messages": messages,
            "temperature": 0.7,
            "max_tokens": 600,
            "stream": True,
        }
        try:
            with httpx.Client(timeout=60) as client:
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
                        except json.JSONDecodeError:
                            pass
        except Exception as e:
            print(f"LLM STREAM ERROR: {e}")
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
        messages = [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "system", "content": f"Kontekst məlumatı:\n{context}"},
            {"role": "user", "content": question},
        ]
        return self.chat(messages)
