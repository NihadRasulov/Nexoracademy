"""
SQLAlchemy ORM models.
"""
from __future__ import annotations

from datetime import datetime

from sqlalchemy import (
    Column,
    DateTime,
    Integer,
    String,
    Text,
    Boolean,
    Index,
    func,
)
from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    pass


class Lead(Base):
    __tablename__ = "leads"

    id = Column(Integer, primary_key=True, autoincrement=True)
    lead_ref = Column(String(64), unique=True, nullable=False)
    name = Column(String(128), nullable=False, default="")
    phone = Column(String(32), nullable=False, default="")
    email = Column(String(128), nullable=True, default="")
    interest = Column(String(64), nullable=True, default="")
    level = Column(String(32), nullable=True, default="")
    note = Column(Text, nullable=True, default="")
    source = Column(String(32), nullable=False, default="chatbot")
    session_id = Column(String(128), nullable=True)
    user_id = Column(String(128), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    __table_args__ = (
        Index("ix_leads_phone", "phone"),
        Index("ix_leads_session", "session_id"),
        Index("ix_leads_user", "user_id"),
        Index("ix_leads_created", "created_at"),
    )

    def to_dict(self) -> dict:
        return {
            "id": self.lead_ref,
            "name": self.name,
            "phone": self.phone,
            "email": self.email,
            "interest": self.interest,
            "level": self.level,
            "note": self.note,
            "source": self.source,
            "sessionId": self.session_id,
            "userId": self.user_id,
            "createdAt": self.created_at.isoformat() if self.created_at else None,
        }


class ConversationLog(Base):
    __tablename__ = "conversation_logs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(128), nullable=False)
    user_id = Column(String(128), nullable=True)
    state_before = Column(String(64), nullable=True)
    state_after = Column(String(64), nullable=True)
    user_message = Column(Text, nullable=True)
    assistant_reply = Column(Text, nullable=True)
    llm_called = Column(Boolean, default=False, nullable=False)
    llm_latency_ms = Column(Integer, nullable=True)
    fallback_hit = Column(Boolean, default=False, nullable=False)
    retry_count = Column(Integer, default=0, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    __table_args__ = (
        Index("ix_conv_session", "session_id"),
        Index("ix_conv_created", "created_at"),
    )
