from fastapi import APIRouter
from models.schemas import LeadRequest, LeadResponse
from core.lead_service import add_lead, get_all_leads

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
    return LeadResponse(success=True, lead=lead)


@router.get("/lead")
async def list_leads():
    return {"count": len(get_all_leads()), "leads": get_all_leads()}
