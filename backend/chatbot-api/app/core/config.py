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



    def validate(self) -> list[str]:
        """Return a list of warnings; in production, critical issues become errors."""
        warnings = []
        errors = []
        if not self.redis_enabled:
            msg = "Redis disabled – sessions will be in-memory only"
            if self.is_production:
                errors.append(msg + " (FATAL in production)")
            else:
                warnings.append(msg)
        if "sqlite" in self.database_url and self.is_production:
            errors.append("SQLite detected in production – use PostgreSQL (FATAL)")
        if self.is_production and errors:
            import logging
            log = logging.getLogger("nexora.config")
            for e in errors:
                log.critical("config_error msg=%s", e)
            sys.exit(1)
        return warnings


# Singleton – import this everywhere
settings = Config()
