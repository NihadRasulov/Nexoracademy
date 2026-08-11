import re

PHONE_REGEX = re.compile(r"\+994\d{9}")

NAME_HINT = re.compile(
    r"(?:mənim adım|adım|adim|mən |adım |adim )"
    r"([A-ZƏİÖÜÇŞĞ][a-zəıöüçşğ]+(?:\s+[A-ZƏİÖÜÇŞĞ][a-zəıöüçşğ]+)?)",
    re.IGNORECASE,
)

KEYWORD_MAP = {
    "price": [
        "qiymət", "qiymet", "neçəyə", "neceye", "pul", "haqq", "azn",
        "ödəniş", "odenis", "nə qədər", "ne qeder", "ucuz", "bahali",
    ],
    "installment": ["taksit", "hissə", "hisse", "bank", "faizsiz", "kredit"],
    "scholarship": ["təqaüd", "teqaud", "endirim", "kampaniya", "pulsuz"],
    "cohort": ["axın", "axin", "başlayır", "baslayir", "nə vaxt", "ne vaxt", "tarix", "start"],
    "format": ["onlayn", "online", "oflayn", "offline", "harada", "məkan", "mekan", "unvan"],
    "registration": ["qeydiyyat", "yazıl", "yazil", "qoşul", "qosul", "demo"],
    "mentor": ["mentor", "dəstək", "destek", "karyera"],
    "certificate": ["sertifikat", "diplom", "sertifikat", "oscp", "aws", "ccna"],
    "contact": ["əlaqə", "elaqe", "telefon", "zəng", "zeng", "ünvan", "unvan"],
    "employment": ["iş", "is", "işə", "ise", "employment", "maaş", "maas"],
}


def detect_intent(text: str) -> str:
    lower = text.lower().strip()

    if PHONE_REGEX.search(text):
        return "lead_phone"

    if NAME_HINT.search(text):
        return "lead_name"

    if not lower or len(lower) < 3:
        return "greeting"

    if any(kw in lower for kw in ["hansı kurs", "tövsiyə", "tovsiye", "məsləhət", "meslehet",
                                   "maraqlanıram", "öyrənmək istəyirəm", "baslamaq istəyirem",
                                   "proqram", "kiber", "şəbəkə", "sebeke", "soc"]):
        return "recommendation"

    if any(kw in lower for kw in KEYWORD_MAP["price"]):
        return "question"

    if any(kw in lower for kw in KEYWORD_MAP["cohort"]):
        return "question"

    if any(kw in lower for kw in KEYWORD_MAP["registration"]):
        return "registration"

    return "question"


def extract_phone(text: str) -> str | None:
    match = PHONE_REGEX.search(text.replace(" ", "").replace("-", ""))
    if match:
        return match.group(0)
    return None


def extract_name(text: str) -> str | None:
    match = NAME_HINT.search(text)
    if match:
        return match.group(1)
    words = text.strip().split()
    if len(words) >= 2 and all(w[0].isupper() if w else False for w in words[:2]):
        return " ".join(words[:2])
    return None


def is_blocked(text: str) -> bool:
    patterns = [
        r"ignore (all|previous|the) (instructions|rules)",
        r"system prompt",
        r"you are now",
        r"act as",
        r"jailbreak",
        r"təlimatları unut",
        r"qaydaları unut",
        r"rolunu dəyiş",
        r"sən indi",
    ]
    lower = text.lower()
    for p in patterns:
        if re.search(p, lower):
            return True
    return False
