from pydantic import BaseModel, Field
from typing import Optional, Literal


class ChatRequest(BaseModel):
    message: str
    sessionId: str = ""
    conversationId: str | None = None
    userId: str | None = None


class ActionButton(BaseModel):
    type: Literal["button"] = "button"
    label: str
    value: str


class CourseCard(BaseModel):
    id: str
    name: str
    price: Optional[float] = None
    category: str
    level: str
    tools: list[str] = Field(default_factory=list)
    instructor: str = ""
    schedule: dict = Field(default_factory=dict)


class ChatResponse(BaseModel):
    reply: str
    state: str
    actions: list[ActionButton] = Field(default_factory=list)
    courses: list[CourseCard] = Field(default_factory=list)
    capture: str = "none"


class LeadRequest(BaseModel):
    name: str
    phone: str
    email: Optional[str] = ""
    interest: Optional[str] = ""
    level: Optional[str] = ""
    note: Optional[str] = ""
    source: Optional[str] = "chatbot"
    sessionId: Optional[str] = None


class LeadResponse(BaseModel):
    success: bool
    lead: dict


class IndexDocument(BaseModel):
    id: str
    text: str
    metadata: dict = Field(default_factory=dict)
