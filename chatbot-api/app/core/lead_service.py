from datetime import datetime

leads: list[dict] = []


def add_lead(data: dict) -> dict:
    entry = {
        "id": f"lead-{datetime.now().timestamp():.0f}-{id(data):x}",
        "name": data.get("name", ""),
        "phone": data.get("phone", ""),
        "interest": data.get("interest", ""),
        "level": data.get("level", ""),
        "email": data.get("email", ""),
        "note": data.get("note", ""),
        "source": data.get("source", "chatbot"),
        "sessionId": data.get("sessionId", ""),
        "createdAt": datetime.now().isoformat(),
    }
    leads.append(entry)
    print(f"[LEAD] {entry['name']} | {entry['phone']} | {entry['interest']}")
    return entry


def get_all_leads() -> list[dict]:
    return list(leads)


def count_leads() -> int:
    return len(leads)
