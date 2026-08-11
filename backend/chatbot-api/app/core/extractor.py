import re

PHONE_REGEX = re.compile(r"\+994\d{9}")


def extract_phone(text: str) -> str | None:
    cleaned = text.replace(" ", "").replace("-", "")
    match = PHONE_REGEX.search(cleaned)
    if match:
        return match.group(0)
    return None


def extract_name(text: str) -> str | None:
    hint = re.search(
        r"(?:adım|adim|adım |adim |mən |mənim adım |menim adim )"
        r"([A-ZƏİÖÜÇŞĞ][a-zəıöüçşğ]+(?:\s+[A-ZƏİÖÜÇŞĞ][a-zəıöüçşğ]+)?)",
        text,
        re.IGNORECASE,
    )
    if hint:
        return hint.group(1)
    name_match = re.match(
        r"^([A-ZƏİÖÜÇŞĞ][a-zəıöüçşğ]+)\s+([A-ZƏİÖÜÇŞĞ][a-zəıöüçşğ]+)",
        text.strip(),
    )
    if name_match:
        return f"{name_match.group(1)} {name_match.group(2)}"
    words = text.strip().split()
    if len(words) >= 2:
        first = words[0].capitalize()
        if first and first[0].isupper():
            return f"{first} {words[1].capitalize()}"
    return None
