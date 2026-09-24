#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Build an Arabic-only, source-traceable Sunnah catalogue from Sahih Bukhari and Sahih Muslim."""

import json
import re
import sqlite3
from pathlib import Path

DB = Path("app/src/main/assets/databases/sunnah.db")
TARGET_TOTAL = 1200
SOURCE_DIRS = [
    ("SAHIH_BUKHARI", Path("data/raw/chapters/bukhari"), "صحيح البخاري"),
    ("SAHIH_MUSLIM", Path("data/raw/chapters/muslim"), "صحيح مسلم"),
]

def normalize(text):
    text = text or ""
    text = re.sub(r"[\u064B-\u065F\u0670\u0640\u06D6-\u06ED]", "", text)
    text = re.sub(r"[إأآٱ]", "ا", text)
    text = re.sub(r"ى", "ي", text)
    return re.sub(r"\s+", " ", text).strip()

def category_for(text):
    hay = normalize(text)
    groups = [
        ("الصلاة والعبادات", ["صلاة", "سجود", "ركوع", "أذان", "وضوء", "تيمم"]),
        ("القرآن والذكر", ["قرآن", "ذكر", "دعاء", "استغفار", "تسبيح"]),
        ("الصيام", ["صوم", "صيام", "رمضان"]),
        ("الحج والعمرة", ["حج", "عمرة", "إحرام", "طواف", "عرفات"]),
        ("الطعام والشراب", ["طعام", "أكل", "شراب", "شرب"]),
        ("الطهارة والنظافة", ["طهارة", "غسل", "سواك", "نجاسة"]),
        ("الآداب والتعامل", ["أدب", "سلام", "عطس", "جار", "رحم", "كلام", "رفق"]),
        ("البر والإحسان", ["صدقة", "إحسان", "يتيم", "مسكين", "فقير"]),
        ("العلم والتعليم", ["علم", "تعلم", "كتاب", "قراءة"]),
        ("الأسرة", ["نكاح", "زوج", "نساء", "أهل", "ولد", "أب", "أم"]),
        ("المعاملات", ["بيع", "شراء", "دين", "مال", "تجارة"]),
        ("اللباس والهيئة", ["لباس", "ثوب", "شعر", "نعال"]),
        ("النوم والاستيقاظ", ["نوم", "فراش", "استيقظ"]),
        ("السفر", ["سفر", "سافر", "طريق"]),
        ("الصحة والمرض", ["مرض", "دواء", "طب", "علاج"]),
    ]
    for name, words in groups:
        if any(word in hay for word in words):
            return name
    return "هدي نبوي عام"

def difficulty_for(text):
    hay = normalize(text)
    if any(w in hay for w in ["سلام", "عطس", "طعام", "شراب", "جار", "كلمة", "لباس"]):
        return 1
    if any(w in hay for w in ["نوم", "وضوء", "أذان", "ذكر", "دعاء", "سواك"]):
        return 2
    if any(w in hay for w in ["صلاة", "صيام", "قرآن", "علم", "حج", "عمرة"]):
        return 3
    if any(w in hay for w in ["قيام", "اعتكاف", "زكاة", "صدقة"]):
        return 4
    return 3

def minutes_for(level):
    return {1: 2, 2: 4, 3: 8, 4: 15, 5: 25}[level]

def load_candidates():
    result = []
    seen = set()
    action_markers = [
        "قال رسول الله", "قال النبي", "كان رسول الله", "كان النبي",
        "رأيت رسول الله", "رأيت النبي", "نهى رسول الله", "أمر رسول الله",
        "إذا", "من كان", "كان إذا"
    ]

    for collection, directory, arabic_name in SOURCE_DIRS:
        for path in sorted(directory.glob("*.json"), key=lambda p: int(p.stem)):
            data = json.loads(path.read_text(encoding="utf-8"))
            chapter_id = int(path.stem)
            for hadith in data.get("hadiths", []):
                arabic = (hadith.get("arabic") or "").strip()
                if len(arabic) < 40 or not any(m in arabic for m in action_markers):
                    continue
                key = normalize(arabic)
                if not key or key in seen:
                    continue
                seen.add(key)
                raw_id = int(hadith.get("id", 0))
                hadith_number = int(hadith.get("idInBook", raw_id))
                source = f"{arabic_name} — حديث رقم {hadith_number} — الباب رقم {chapter_id}"
                result.append({
                    "collection": collection,
                    "book": arabic_name,
                    "chapter": f"الباب رقم {chapter_id}",
                    "hadith_number": hadith_number,
                    "raw_id": raw_id,
                    "chapter_id": chapter_id,
                    "book_id": int(hadith.get("bookId", 0)),
                    "arabic": arabic,
                    "source": source,
                })
    return result

def main():
    if not DB.exists():
        raise SystemExit(f"Missing database: {DB}")

    con = sqlite3.connect(DB)
    cur = con.cursor()

    existing_count = cur.execute("SELECT COUNT(*) FROM Sunnah").fetchone()[0]
    if existing_count >= TARGET_TOTAL:
        print(f"Already expanded: {existing_count}")
        con.close()
        return

    existing_texts = {normalize(r[0]) for r in cur.execute("SELECT arabicText FROM Hadith")}
    max_hadith_id = cur.execute("SELECT COALESCE(MAX(id), 0) FROM Hadith").fetchone()[0]
    max_sunnah_id = cur.execute("SELECT COALESCE(MAX(id), 0) FROM Sunnah").fetchone()[0]

    candidates = [x for x in load_candidates() if normalize(x["arabic"]) not in existing_texts]
    needed = TARGET_TOTAL - existing_count
    if len(candidates) < needed:
        raise SystemExit(f"Only {len(candidates)} usable narrations found; {needed} required.")

    selected = candidates[:needed]
    selected.sort(key=lambda x: (difficulty_for(x["arabic"]), x["collection"], x["raw_id"]))

    hadith_rows = []
    sunnah_rows = []
    for offset, item in enumerate(selected, start=1):
        hid = max_hadith_id + offset
        sid = max_sunnah_id + offset
        level = difficulty_for(item["arabic"])
        category = category_for(item["arabic"])
        title = f"هدي نبوي: {category}"
        description = (
            "إدراج مبني مباشرة على نص الحديث المذكور في المصدر، "
            "دون إضافة حكم شرعي من التطبيق. اقرأ النص الكامل والمرجع قبل التطبيق."
        )
        hadith_rows.append((
            hid, item["collection"], item["book"], item["chapter"],
            item["hadith_number"], "راوي الحديث", item["arabic"], item["source"],
            "صحيح", 0, None, item["raw_id"], item["chapter_id"], item["book_id"]
        ))
        sunnah_rows.append((
            sid, title, description, hid, level, category,
            minutes_for(level), sid, 1
        ))

    cur.executemany(
        "INSERT INTO Hadith (id, collection, book, chapter, hadithNumber, narrator, arabicText, sourceReference, authenticity, isAgreedUpon, linkedHadithId, rawId, chapterId, bookId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        hadith_rows
    )
    cur.executemany(
        "INSERT INTO Sunnah (id, title, description, hadithId, difficulty, category, estimatedMinutes, orderIndex, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
        sunnah_rows
    )

    cur.execute("INSERT INTO HadithFts(HadithFts) VALUES('rebuild')")
    con.commit()

    total = cur.execute("SELECT COUNT(*) FROM Sunnah").fetchone()[0]
    hadith_total = cur.execute("SELECT COUNT(*) FROM Hadith").fetchone()[0]
    integrity = cur.execute("PRAGMA integrity_check").fetchone()[0]
    distribution = cur.execute("SELECT difficulty, COUNT(*) FROM Sunnah GROUP BY difficulty ORDER BY difficulty").fetchall()
    con.close()

    print(f"Expanded Sunnahs: {total}")
    print(f"Hadith rows: {hadith_total}")
    print(f"Difficulty distribution: {distribution}")
    print(f"SQLite integrity: {integrity}")

if __name__ == "__main__":
    main()
