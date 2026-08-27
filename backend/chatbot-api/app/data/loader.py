import json
import os
from pathlib import Path


DATA_DIR = Path(__file__).resolve().parent.parent / "data_files"


def load_courses() -> list[dict]:
    path = DATA_DIR / "courses.json"
    if not path.exists():
        return []
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def load_knowledge() -> list[dict]:
    path = DATA_DIR / "knowledge.json"
    if not path.exists():
        return []
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def load_reviews() -> list[dict]:
    path = DATA_DIR / "reviews.json"
    if not path.exists():
        return []
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def load_all_data() -> dict:
    return {
        "courses": load_courses(),
        "knowledge": load_knowledge(),
        "reviews": load_reviews(),
    }
