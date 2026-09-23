import sqlite3
import os

DB = "app/src/main/assets/databases/sunnah.db"

SCHEMAS = {
    "Hadith": """
        CREATE TABLE {table} (
            id INTEGER PRIMARY KEY NOT NULL,
            collection TEXT NOT NULL,
            book TEXT NOT NULL,
            chapter TEXT NOT NULL,
            hadithNumber INTEGER NOT NULL,
            narrator TEXT NOT NULL,
            arabicText TEXT NOT NULL,
            sourceReference TEXT NOT NULL,
            authenticity TEXT NOT NULL,
            isAgreedUpon INTEGER NOT NULL,
            linkedHadithId INTEGER,
            rawId INTEGER NOT NULL,
            chapterId INTEGER NOT NULL,
            bookId INTEGER NOT NULL
        )
    """,
    "Sunnah": """
        CREATE TABLE {table} (
            id INTEGER PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            hadithId INTEGER NOT NULL,
            difficulty INTEGER NOT NULL,
            category TEXT NOT NULL,
            estimatedMinutes INTEGER NOT NULL,
            orderIndex INTEGER NOT NULL,
            isActive INTEGER NOT NULL
        )
    """,
    "UserProgress": """
        CREATE TABLE {table} (
            id INTEGER PRIMARY KEY NOT NULL,
            currentSunnahId INTEGER NOT NULL,
            completedSunnahs TEXT NOT NULL,
            currentStreak INTEGER NOT NULL,
            longestStreak INTEGER NOT NULL,
            lastCompletedDate TEXT,
            startedDate TEXT NOT NULL
        )
    """,
    "PdfBook": """
        CREATE TABLE {table} (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            filename TEXT NOT NULL,
            size INTEGER NOT NULL,
            addedDate INTEGER NOT NULL,
            isBuiltin INTEGER NOT NULL
        )
    """,
}

def repair_table(con, table, create_sql):
    info = con.execute(f"PRAGMA table_info({table})").fetchall()
    if not info:
        raise RuntimeError(f"Missing required table: {table}")

    new_table = f"{table}__room_fix"
    con.execute(f"DROP TABLE IF EXISTS {new_table}")
    con.execute(create_sql.format(table=new_table))

    columns = [row[1] for row in info]
    quoted = ", ".join(f'"{c}"' for c in columns)
    con.execute(
        f'INSERT INTO "{new_table}" ({quoted}) SELECT {quoted} FROM "{table}"'
    )
    con.execute(f'DROP TABLE "{table}"')
    con.execute(f'ALTER TABLE "{new_table}" RENAME TO "{table}"')

def main():
    if not os.path.exists(DB):
        raise SystemExit(f"Database not found: {DB}")

    con = sqlite3.connect(DB)
    con.execute("PRAGMA foreign_keys=OFF")

    for table, schema in SCHEMAS.items():
        repair_table(con, table, schema)

    con.commit()
    result = con.execute("PRAGMA integrity_check").fetchone()[0]
    if result != "ok":
        raise SystemExit(f"SQLite integrity check failed: {result}")

    for table in SCHEMAS:
        rows = con.execute(f"PRAGMA table_info({table})").fetchall()
        id_row = next(row for row in rows if row[1] == "id")
        if id_row[3] != 1 or id_row[5] != 1:
            raise SystemExit(f"{table}.id is not Room-compatible: {id_row}")

    con.close()
    print("Bundled database schema repaired for Room.")

if __name__ == "__main__":
    main()
