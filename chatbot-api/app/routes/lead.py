"""
Lead routes – now backed by SQLAlchemy persistence.
"""
from __future__ import annotations

import logging

from fastapi import APIRouter, Query

from models.schemas import LeadRequest, LeadResponse
from core.lead_service import add_lead, get_all_leads, count_leads

logger = logging.getLogger("nexora.route.lead")

router = APIRouter()


@router.post("/lead")
async def create_lead(req: LeadRequest):
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

    logger.info("lead_created_via_api name=%s phone=%s", req.name, phone)
    return LeadResponse(success=True, lead=lead)


@router.get("/lead")
async def list_leads(limit: int = Query(default=100, le=500)):
    all_leads = get_all_leads()
    return {
        "count": len(all_leads),
        "leads": all_leads[:limit],
    }


@router.get("/lead/count")
async def lead_count():
    return {"count": count_leads()}
