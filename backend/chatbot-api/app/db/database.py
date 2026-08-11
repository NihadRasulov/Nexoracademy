"""
Database engine and session factory.
Supports both PostgreSQL (production) and SQLite (development / fallback).
"""
from __future__ import annotations

import logging
from contextlib import contextmanager
from typing import Generator

from sqlalchemy import create_engine, event, text
from sqlalchemy.orm import sessionmaker, Session

from core.config import settings
from db.models import Base

logger = logging.getLogger("nexora.db")

_engine = None
_SessionLocal: sessionmaker | None = None


def init_db(database_url: str | None = None) -> None:
    global _engine, _SessionLocal

    url = database_url or settings.database_url

    connect_args = {}
    if url.startswith("sqlite"):
        connect_args["check_same_thread"] = False

    _engine = create_engine(
        url,
        echo=settings.db_echo,
        connect_args=connect_args,
        pool_pre_ping=True,
        pool_recycle=3600,
    )

    if url.startswith("sqlite"):
        @event.listens_for(_engine, "connect")
        def set_sqlite_pragma(dbapi_conn, _connection_record):
            cursor = dbapi_conn.cursor()
            cursor.execute("PRAGMA journal_mode=WAL")
            cursor.close()

    Base.metadata.create_all(bind=_engine)
    _SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=_engine)
    logger.info("db_init url=%s", url.split("@")[-1] if "@" in url else url)


@contextmanager
def get_db() -> Generator[Session, None, None]:
    if _SessionLocal is None:
        raise RuntimeError("Database not initialised. Call init_db() first.")
    session = _SessionLocal()
    try:
        yield session
        session.commit()
    except Exception:
        session.rollback()
        raise
    finally:
        session.close()


def health_check() -> bool:
    try:
        if _engine is None:
            return False
        with _engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        return True
    except Exception as exc:
        logger.error("db_health_check_failed error=%s", exc)
        return False
