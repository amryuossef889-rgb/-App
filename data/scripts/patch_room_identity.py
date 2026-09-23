import os
import sqlite3
import sys

db_path = "app/src/main/assets/databases/sunnah.db"
identity_hash = os.environ.get("ROOM_IDENTITY_HASH", "").strip()

if not identity_hash:
    print("ERROR: ROOM_IDENTITY_HASH is empty")
    sys.exit(1)

if not os.path.exists(db_path):
    print(f"ERROR: Database not found: {db_path}")
    sys.exit(1)

conn = sqlite3.connect(db_path)
cur = conn.cursor()

cur.execute("""
CREATE TABLE IF NOT EXISTS room_master_table (
    id INTEGER PRIMARY KEY,
    identity_hash TEXT
)
""")
cur.execute("DELETE FROM room_master_table")
cur.execute(
    "INSERT INTO room_master_table (id, identity_hash) VALUES (42, ?)",
    (identity_hash,)
)
conn.commit()

row = cur.execute(
    "SELECT id, identity_hash FROM room_master_table LIMIT 1"
).fetchone()
print("Room identity hash patched:", row)

conn.close()
