from __future__ import annotations

import sys
import unittest
from pathlib import Path
from unittest.mock import patch


APP_DIR = Path(__file__).resolve().parents[1] / "app"
sys.path.insert(0, str(APP_DIR))

from data import loader


class CatalogLoaderTests(unittest.TestCase):
    def setUp(self):
        loader._catalog_cache["courses"] = None
        loader._catalog_cache["expires_at"] = 0.0

    def test_java_course_is_adapted_for_chat_cards(self):
        course = loader._normalise_platform_course(
            {
                "id": "8ca246bc-b0c9-40d8-a36c-7ea0d5441c89",
                "categoryId": 7,
                "title": "Java Backend",
                "difficulty": "BEGINNER",
                "deliveryFormat": "ONLINE",
                "durationWeeks": 12,
                "basePrice": "850.00",
                "targetAudience": "Yeni başlayanlar, tələbələr",
                "instructor": {"fullName": "Aysel Əliyeva", "bio": "Senior Java Engineer"},
                "content": {
                    "tools": ["Java", "Spring Boot"],
                    "schedule": {"days": "B.e / Ç", "time": "19:00"},
                },
            },
            {"7": "Proqramlaşdırma"},
        )

        self.assertEqual(course["direction"], "Proqramlaşdırma")
        self.assertEqual(course["level"], "Başlanğıc")
        self.assertEqual(course["format"], "online")
        self.assertEqual(course["priceAzn"], 850)
        self.assertEqual(course["instructor"]["name"], "Aysel Əliyeva")
        self.assertEqual(course["schedule"]["time"], "19:00")

    def test_live_catalog_is_cached(self):
        expected = [{"id": "java", "title": "Java Backend"}]
        with patch.object(loader, "_fetch_platform_courses", return_value=expected) as fetch:
            self.assertEqual(loader.load_courses(), expected)
            self.assertEqual(loader.load_courses(), expected)
        fetch.assert_called_once()

    def test_static_catalog_is_used_when_java_is_unavailable(self):
        fallback = [{"id": "fallback", "title": "Fallback course"}]
        with (
            patch.object(loader, "_fetch_platform_courses", side_effect=ValueError("offline")),
            patch.object(loader, "_load_json", return_value=fallback) as load_static,
        ):
            self.assertEqual(loader.load_courses(), fallback)
        load_static.assert_called_once_with("courses.json")


if __name__ == "__main__":
    unittest.main()
