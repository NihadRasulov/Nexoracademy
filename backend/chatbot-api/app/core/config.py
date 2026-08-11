"""
Centralised configuration for the Nexora chatbot.
All settings are read from environment variables with safe defaults.
Redis and PostgreSQL are optional – the system degrades gracefully without them.
"""
from __future__ import annotations
import os
from dataclasses import dataclass, field


@dataclass
class Config:
    # ── LLM ─────────────────────────────────────────────────────────────────
    openrouter_api_key: str = field(default_factory=lambda: os.environ.get("OPENROUTER_API_KEY", ""))
    openrouter_model: str = field(default_factory=lambda: os.environ.get("OPENROUTER_MODEL", "openai/gpt-4o-mini"))
    openrouter_url: str = "https://openrouter.ai/api/v1/chat/completions"

    # LLM call tuning
    llm_timeout_s: float = float(os.environ.get("LLM_TIMEOUT_S", "12"))
    llm_max_tokens: int = int(os.environ.get("LLM_MAX_TOKENS", "400"))
    llm_temperature: float = float(os.environ.get("LLM_TEMPERATURE", "0.7"))
    llm_max_retries: int = int(os.environ.get("LLM_MAX_RETRIES", "3"))
    llm_backoff_base: float = float(os.environ.get("LLM_BACKOFF_BASE", "1.0"))

    # Circuit breaker
    cb_failure_threshold: int = int(os.environ.get("CB_FAILURE_THRESHOLD", "5"))
    cb_recovery_window_s: int = int(os.environ.get("CB_RECOVERY_WINDOW_S", "30"))

    # ── Redis ────────────────────────────────────────────────────────────────
    redis_url: str = field(default_factory=lambda: os.environ.get("REDIS_URL", "redis://localhost:6379/0"))
    redis_enabled: bool = field(default_factory=lambda: os.environ.get("REDIS_ENABLED", "true").lower() == "true")

    # Session config
    session_ttl_s: int = int(os.environ.get("SESSION_TTL_S", "3600"))       # 1 hour idle expiry
    session_history_max: int = int(os.environ.get("SESSION_HISTORY_MAX", "40"))  # turns persisted
    session_history_context: int = int(os.environ.get("SESSION_HISTORY_CONTEXT", "6"))  # turns sent to LLM

    # Rate limiter
    rate_limit_max: int = int(os.environ.get("RATE_LIMIT_MAX", "40"))
    rate_limit_window_s: int = int(os.environ.get("RATE_LIMIT_WINDOW_S", "60"))

    # ── Database ─────────────────────────────────────────────────────────────
    database_url: str = field(
        default_factory=lambda: os.environ.get(
            "DATABASE_URL", "sqlite:///./nexora_leads.db"
        )
    )
    db_echo: bool = field(default_factory=lambda: os.environ.get("DB_ECHO", "false").lower() == "true")

    # ── Application ──────────────────────────────────────────────────────────
    port: int = int(os.environ.get("PORT", "8000"))
    environment: str = field(default_factory=lambda: os.environ.get("ENVIRONMENT", "development"))
    log_level: str = field(default_factory=lambda: os.environ.get("LOG_LEVEL", "INFO"))

    # Context / RAG
    rag_top_k: int = int(os.environ.get("RAG_TOP_K", "3"))
    context_max_courses: int = int(os.environ.get("CONTEXT_MAX_COURSES", "10"))

    # Metrics
    metrics_enabled: bool = field(
        default_factory=lambda: os.environ.get("METRICS_ENABLED", "true").lower() == "true"
    )

    @property
    def is_production(self) -> bool:
        return self.environment == "production"

    @property
    def llm_available(self) -> bool:
        return bool(self.openrouter_api_key)

    def validate(self) -> list[str]:
        """Return a list of warnings about missing/risky configuration."""
        warnings = []
        if not self.openrouter_api_key:
            warnings.append("OPENROUTER_API_KEY not set – LLM will be unavailable")
        if not self.redis_enabled:
            warnings.append("Redis disabled – sessions will be in-memory only (not suitable for production)")
        if "sqlite" in self.database_url and self.is_production:
            warnings.append("SQLite detected in production – use PostgreSQL")
        return warnings


# Singleton – import this everywhere
settings = Config()
