#!/usr/bin/env python3
"""
Prepare the bundled Room database for first-run initialization.

The checked-in database previously contained a hand-written room_master_table
with a fake identity hash. Room rejects such a prepackaged database before the
first screen can open. Removing that table lets Room validate the actual schema
and write its own identity hash on first open.
"""
from pathlib import Path
import sqlite3

DB_PATH = Path("app/src/main/assets/databases/sunnah.db")

def main() -> None:
    if not DB_PATH.exists():
        raise SystemExit(f"Database not found: {DB_PATH}")

    conn = sqlite3.connect(DB_PATH)
    try:
        conn.execute("DROP TABLE IF EXISTS room_master_table")
        conn.execute("PRAGMA user_version = 1")
        conn.commit()

        required = {"Hadith", "Sunnah", "UserProgress", "PdfBook"}
        actual = {
            row[0]
            for row in conn.execute(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'"
            )
        }
        missing = required - actual
        if missing:
            raise RuntimeError(f"Missing required Room tables: {sorted(missing)}")

        result = conn.execute("PRAGMA integrity_check").fetchone()
        if not result or result[0] != "ok":
            raise RuntimeError(f"SQLite integrity check failed: {result}")
    finally:
        conn.close()

    print(f"Prepared Room database: {DB_PATH}")

if __name__ == "__main__":
    main()
