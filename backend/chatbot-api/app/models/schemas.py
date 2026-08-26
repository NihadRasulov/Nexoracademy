from pydantic import BaseModel, Field, PrivateAttr
from typing import Optional, Literal


class ChatRequest(BaseModel):
    message: str = Field(..., max_length=4000)
    conversationId: str | None = Field(default=None, max_length=128)
    sessionId: str | None = Field(default=None, max_length=128)


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
    _llm_called: bool = PrivateAttr(default=False)


class IndexDocument(BaseModel):
    id: str
    text: str
    metadata: dict = Field(default_factory=dict)
