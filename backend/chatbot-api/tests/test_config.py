from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch


APP_DIR = Path(__file__).resolve().parents[1] / "app"
sys.path.insert(0, str(APP_DIR))

from core.config import Config


class ConfigTests(unittest.TestCase):
    def test_development_defaults_to_sqlite(self):
        with patch.dict(os.environ, {"ENVIRONMENT": "development"}, clear=True):
            config = Config()

        self.assertEqual(config.database_url, "sqlite:///./nexora_leads.db")

    def test_production_derives_postgres_url_from_compose_variables(self):
        with patch.dict(
            os.environ,
            {
                "ENVIRONMENT": "production",
                "DB_HOST": "postgres",
                "DB_PORT": "5432",
                "DB_NAME": "nexora_academy",
                "DB_USER": "nexora_app",
                "DB_PASSWORD": "secret@word",
                "REDIS_ENABLED": "true",
            },
            clear=True,
        ):
            config = Config()

        self.assertEqual(
            config.database_url,
            "postgresql+psycopg2://nexora_app:secret%40word@postgres:5432/nexora_academy",
        )
        self.assertEqual(config.validate(), [])

    def test_explicit_database_url_takes_precedence(self):
        explicit_url = "postgresql+psycopg2://app:password@database:5432/chatbot"
        with patch.dict(
            os.environ,
            {
                "ENVIRONMENT": "production",
                "CHATBOT_DATABASE_URL": explicit_url,
                "REDIS_ENABLED": "true",
            },
            clear=True,
        ):
            config = Config()

        self.assertEqual(config.database_url, explicit_url)

    def test_legacy_database_url_does_not_override_production_compose_database(self):
        with patch.dict(
            os.environ,
            {
                "ENVIRONMENT": "production",
                "DATABASE_URL": "sqlite:///./legacy.db",
                "DB_HOST": "postgres",
                "DB_PORT": "5432",
                "DB_NAME": "nexora_academy",
                "DB_USER": "nexora_app",
                "DB_PASSWORD": "password",
                "REDIS_ENABLED": "true",
            },
            clear=True,
        ):
            config = Config()

        self.assertTrue(config.database_url.startswith("postgresql+psycopg2://"))
        self.assertEqual(config.validate(), [])

    def test_production_rejects_explicit_sqlite_without_name_error(self):
        with patch.dict(
            os.environ,
            {
                "ENVIRONMENT": "production",
                "CHATBOT_DATABASE_URL": "sqlite:///./invalid.db",
                "REDIS_ENABLED": "true",
            },
            clear=True,
        ):
            config = Config()

        with self.assertRaises(SystemExit):
            config.validate()


if __name__ == "__main__":
    unittest.main()
