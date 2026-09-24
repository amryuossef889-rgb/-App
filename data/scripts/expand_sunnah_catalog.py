#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Expand the bundled Sunnah catalogue from the pinned Sahih Bukhari/Muslim datasets.

The app deliberately keeps the original curated entries and adds 1,000+ directly
traceable hadith-based entries. Every generated entry keeps the original Arabic
hadith text and an Arabic source reference. The difficulty value is an app
organization aid only; it is not a religious ruling or a grading of the hadith.
"""

import json
import re
import sqlite3
from generate_complete_db import sanitize_json
from pathlib import Path

DB = Path("app/src/main/assets/databases/sunnah.db")
TARGET_TOTAL = 1200
SOURCE_DIRS = [
    ("SAHIH_BUKHARI", Path("data/raw/chapters/bukhari"), "صحيح البخاري"),
    ("SAHIH_MUSLIM", Path("data/raw/chapters/muslim"), "صحيح مسلم"),
]#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Expand the bundled Sunnah catalogue from the pinned Sahih Bukhari/Muslim datasets.

The app deliberately keeps the original curated entries and adds 1,000+ directly
traceable hadith-based entries. Every generated entry keeps the original Arabic
hadith text and an Arabic source reference. The difficulty value is an app
organization aid only; it is not a religious ruling or a grading of the hadith.
"""

import json
import re
import sqlite3
from generate_complete_db import sanitize_json
from pathlib import Path

DB = Path("app/src/main/assets/databases/sunnah.db")
TARGET_TOTAL = 1200
SOURCE_FILES = [
    ("SAHIH_BUKHARI", Path("data/raw/bukhari.json"), "صحيح البخاري"),
    ("SAHIH_MUSLIM", Path("data/raw/muslim.json"), "صحيح مسلم"),
]

def normalize(text: str) -> str:
    text = text or ""
    text = re.sub(r"[\u064B-\u065F\u0670\u0640\u06D6-\u06ED]", "", text)
    text = re.sub(r"[إأآٱ]", "ا", text)
    text = re.sub(r"[ى]", "ي", text)
    text = re.sub(r"\s+", " ", text)
    return text.strip()

def clean_chapter(chapter: str) -> str:
    chapter = (chapter or "").strip()
    chapter = re.sub(r"^باب\s*", "", chapter)
    chapter = re.sub(r"\s+", " ", chapter)
    return chapter[:120] if chapter else "هدي نبوي موثق"

def category_for(chapter: str, text: str) -> str:
    hay = normalize(chapter + " " + text)
    groups = [
        ("صلاة وعبادات", ["صلاة", "سجود", "ركوع", "أذان", "وضوء", "تيمم"]),
        ("قرآن وذكر", ["قرآن", "ذكر", "دعاء", "استغفار", "تسبيح"]),
        ("صيام", ["صوم", "صيام", "رمضان"]),
        ("حج وعمرة", ["حج", "عمرة", "إحرام", "طواف", "منى", "عرفات"]),
        ("طعام وشراب", ["طعام", "أكل", "شراب", "شرب", "إناء"]),
        ("طهارة ونظافة", ["طهارة", "غسل", "سواك", "نجاسة"]),
        ("آداب وتعامل", ["أدب", "سلام", "عطس", "جار", "رحم", "كلام", "رفق"]),
        ("بر وإحسان", ["صدقة", "إحسان", "يتيم", "مسكين", "فقير", "رحم"]),
        ("علم وتعليم", ["علم", "تعلم", "كتاب", "قراءة"]),
        ("أسرة ومعاشرة", ["نكاح", "زوج", "نساء", "أهل", "ولد", "أب", "أم"]),
        ("معاملات", ["بيع", "شراء", "دين", "مال", "تجارة"]),
        ("لباس وهيئة", ["لباس", "ثوب", "شعر", "نعال"]),
        ("نوم واستيقاظ", ["نوم", "فراش", "استيقظ"]),
        ("سفر", ["سفر", "سافر", "طريق"]),
        ("طب وصحة", ["مرض", "دواء", "طب", "علاج"]),
    ]
    for name, words in groups:
        if any(w in hay for w in words):
            return name
    return "هدي نبوي عام"

def difficulty_for(chapter: str, text: str) -> int:
    hay = normalize(chapter + " " + text)
    if any(w in hay for w in ["صدقة", "سلام", "عطس", "طعام", "شراب", "جار", "كلمة", "لباس"]):
        return 1
    if any(w in hay for w in ["نوم", "وضوء", "أذان", "ذكر", "دعاء", "سواك", "بيت"]):
        return 2
    if any(w in hay for w in ["صلاة", "صيام", "قرآن", "علم", "حج", "عمرة"]):
        return 3
    if any(w in hay for w in ["قيام", "اعتكاف", "جهاد", "زكاة", "صدقة"]):
        return 4
    return 3

def estimated_minutes(difficulty: int) -> int:
    return {1: 2, 2: 4, 3: 8, 4: 15, 5: 25}[difficulty]

def load_candidates():
    result = []
    seen = set()

    for collection, directory, arabic_name in SOURCE_DIRS:
        for path in sorted(directory.glob("*.json"), key=lambda p: int(p.stem)):
            data = json.loads(path.read_text(encoding="utf-8"))
            metadata = data.get("metadata", {})
            hadiths = data.get("hadiths", [])
            chapter_id = int(path.stem)
            for h in hadiths:
                arabic = (h.get("arabic") or "").strip()
                if len(arabic) < 40:
                    continue

                key = normalize(arabic)
                if not key or key in seen:
                    continue

                action_markers = [
                    "قال رسول الله", "قال النبي", "كان رسول الله", "كان النبي",
                    "رأيت رسول الله", "رأيت النبي", "نهى رسول الله", "أمر رسول الله",
                    "إذا", "من كان", "كان إذا"
                ]
                if not any(marker in arabic for marker in action_markers):
                    continue

                seen.add(key)
                raw_id = int(h.get("id", 0))
                hadith_number = int(h.get("idInBook", raw_id))
                chapter = f"الباب رقم {chapter_id}"
                source = f"{arabic_name} — حديث رقم {hadith_number} — {chapter}"
                result.append({
                    "collection": collection,
                    "book": arabic_name,
                    "chapter": chapter,
                    "hadith_number": hadith_number,
                    "raw_id": raw_id,
                    "chapter_id": chapter_id,
                    "book_id": int(h.get("bookId", 0)),
                    "arabic": arabic,
                    "source": source,
                })
    return result

def main():
    if not DB.exists():
        raise SystemExit(f"Missing database: {DB}")

    con = sqlite3.connect(DB)
    cur = con.cursor()

    existing_sunnahs = cur.execute("SELECT COUNT(*) FROM Sunnah").fetchone()[0]
    if existing_sunnahs >= TARGET_TOTAL:
        print(f"Already expanded: {existing_sunnahs} Sunnahs.")
        con.close()
        return

    existing_texts = {
        normalize(row[0]) for row in cur.execute("SELECT arabicText FROM Hadith").fetchall()
    }
    max_hadith_id = cur.execute("SELECT COALESCE(MAX(id), 0) FROM Hadith").fetchone()[0]
    max_sunnah_id = cur.execute("SELECT COALESCE(MAX(id), 0) FROM Sunnah").fetchone()[0]

    candidates = [c for c in load_candidates() if normalize(c["arabic"]) not in existing_texts]
    needed = TARGET_TOTAL - existing_sunnahs
    if len(candidates) < needed:
        raise SystemExit(f"Only {len(candidates)} usable source narrations found; {needed} required.")

    # Keep source order deterministic, then organize the visible catalogue by
    # difficulty first and original source order second.
    selected = candidates[:needed]
    selected.sort(key=lambda c: (difficulty_for(c["chapter"], c["arabic"]), c["collection"], c["raw_id"]))

    hadith_rows = []
    sunnah_rows = []
    for offset, item in enumerate(selected, start=1):
        hadith_id = max_hadith_id + offset
        sunnah_id = max_sunnah_id + offset
        difficulty = difficulty_for(item["chapter"], item["arabic"])
        category = category_for(item["chapter"], item["arabic"])
        title = f"هدي نبوي: {item['chapter']}"
        description = (
            "هذا الإدراج مبني مباشرة على نص الحديث الموثق في المصدر المذكور، "
            "ويُعرض دون إضافة حكم أو معلومة من خارج الحديث. راجع نص الحديث الكامل والمصدر قبل التطبيق."
        )
        hadith_rows.append((
            hadith_id, item["collection"], item["book"], item["chapter"],
            item["hadith_number"], "راوي الحديث", item["arabic"], item["source"],
            "صحيح", 1 if item["collection"] == "SAHIH_BUKHARI" and False else 0,
            None, item["raw_id"], item["chapter_id"], item["book_id"]
        ))
        sunnah_rows.append((
            sunnah_id, title, description, hadith_id, difficulty, category,
            estimated_minutes(difficulty), sunnah_id, 1
        ))

    cur.executemany("""
        INSERT INTO Hadith (
            id, collection, book, chapter, hadithNumber, narrator, arabicText,
            sourceReference, authenticity, isAgreedUpon, linkedHadithId,
            rawId, chapterId, bookId
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, hadith_rows)

    cur.executemany("""
        INSERT INTO Sunnah (
            id, title, description, hadithId, difficulty, category,
            estimatedMinutes, orderIndex, isActive
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, sunnah_rows)

    # Rebuild the external-content FTS index after adding the source rows.
    cur.execute("INSERT INTO HadithFts(HadithFts) VALUES('rebuild')")
    con.commit()

    total = cur.execute("SELECT COUNT(*) FROM Sunnah").fetchone()[0]
    hadith_total = cur.execute("SELECT COUNT(*) FROM Hadith").fetchone()[0]
    by_difficulty = cur.execute(
        "SELECT difficulty, COUNT(*) FROM Sunnah GROUP BY difficulty ORDER BY difficulty"
    ).fetchall()
    integrity = cur.execute("PRAGMA integrity_check").fetchone()[0]
    con.close()

    print(f"Expanded Sunnahs: {total}")
    print(f"Hadith rows: {hadith_total}")
    print(f"Difficulty distribution: {by_difficulty}")
    print(f"SQLite integrity: {integrity}")

if __name__ == "__main__":
    main()
