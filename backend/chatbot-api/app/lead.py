"""
Lead routes – secured with API key auth and rate limiting.
"""
from __future__ import annotations

import logging
import os

from fastapi import APIRouter, Query, Header, HTTPException, Request, Depends

from models.schemas import LeadRequest, LeadResponse
from core.lead_service import add_lead, get_leads_page, count_leads
from core.rate_limiter import is_rate_limited

logger = logging.getLogger("nexora.route.lead")

router = APIRouter()

LEAD_API_KEY = os.environ.get("LEAD_API_KEY", "")


def _require_api_key(x_api_key: str | None = Header(default=None)):
    if not LEAD_API_KEY:
        raise HTTPException(status_code=503, detail="Lead API not configured")
    if x_api_key != LEAD_API_KEY:
        raise HTTPException(status_code=403, detail="Invalid API key")


@router.post("/lead")
async def create_lead(req: LeadRequest, request: Request):
    client_ip = request.client.host if request.client else "unknown"

    if is_rate_limited(client_ip):
        return {"success": False, "error": "rate_limited"}

    from core.extractor import extract_phone
    phone = extract_phone(req.phone) or req.phone

    lead = add_lead({
        "name": req.name,
        "phone": phone,
        "email": req.email or "",
        "interest": req.interest or "",
        "level": req.level or "",
        "note": req.note or "",
        "source": req.source or "api",
        "sessionId": req.sessionId or "",
    })

    logger.info("lead_created ref=%s source=%s", lead.get("id", "?"), req.source or "api")
    return LeadResponse(success=True, lead=lead)


@router.get("/lead", dependencies=[Depends(_require_api_key)])
async def list_leads(limit: int = Query(default=20, le=100), offset: int = Query(default=0, ge=0)):
    leads = get_leads_page(limit=limit, offset=offset)
    total = count_leads()
    return {
        "count": total,
        "leads": leads,
        "offset": offset,
        "limit": limit,
    }


@router.get("/lead/count", dependencies=[Depends(_require_api_key)])
async def lead_count():
    return {"count": count_leads()}
