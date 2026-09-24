#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Build a source-traceable Arabic hadith catalogue from Sahih Bukhari and Sahih Muslim only."""

import json
import re
import sqlite3
from pathlib import Path

DB = Path("app/src/main/assets/databases/sunnah.db")
TARGET_TOTAL = 1200
SOURCE_DIRS = [
    ("SAHIH_BUKHARI", Path("data/raw/chapters/bukhari"), "صحيح البخاري", 1),
    ("SAHIH_MUSLIM", Path("data/raw/chapters/muslim"), "صحيح مسلم", 2),
]

HARAKAT_RE = r"[\u064B-\u065F\u0670\u06D6-\u06ED]"

def normalize(text):
    text = text or ""
    text = re.sub(HARAKAT_RE, "", text)
    text = re.sub(r"[إأآٱ]", "ا", text)
    text = text.replace("ى", "ي").replace("ـ", "")
    return re.sub(r"\s+", " ", text).strip()

def category_for(text):
    hay = normalize(text)
    groups = [
        ("الصلاة والعبادات", ["صلاة", "سجود", "ركوع", "أذان", "وضوء", "تيمم", "مسجد"]),
        ("القرآن والذكر", ["قرآن", "ذكر", "دعاء", "استغفار", "تسبيح", "تكبير", "تهليل"]),
        ("الصيام", ["صوم", "صيام", "رمضان", "فطر"]),
        ("الحج والعمرة", ["حج", "عمرة", "إحرام", "طواف", "عرفات", "مناسك"]),
        ("الطعام والشراب", ["طعام", "أكل", "شراب", "شرب", "إناء"]),
        ("الطهارة والنظافة", ["طهارة", "غسل", "سواك", "نجاسة", "وضوء", "ثوب"]),
        ("الآداب والتعامل", ["سلام", "عطس", "جار", "رحم", "كلام", "رفق", "مزاح", "ضيف"]),
        ("البر والإحسان", ["صدقة", "إحسان", "يتيم", "مسكين", "فقير", "معروف"]),
        ("العلم والتعليم", ["علم", "تعلم", "كتاب", "قراءة", "فقه"]),
        ("الأسرة", ["نكاح", "زوج", "نساء", "أهل", "ولد", "أب", "أم"]),
        ("المعاملات", ["بيع", "شراء", "دين", "مال", "تجارة", "أمانة"]),
        ("اللباس والهيئة", ["لباس", "ثوب", "شعر", "نعال", "عمامة"]),
        ("النوم والاستيقاظ", ["نوم", "فراش", "استيقظ", "بات"]),
        ("السفر", ["سفر", "سافر", "طريق", "مسافر", "راحلة"]),
        ("الصحة والمرض", ["مرض", "دواء", "طب", "علاج", "حجامة"]),
    ]
    for name, words in groups:
        if any(word in hay for word in words):
            return name
    return "هدي نبوي عام"

def difficulty_for(text):
    hay = normalize(text)
    if any(w in hay for w in ["سلام", "عطس", "طعام", "شراب", "جار", "كلمة", "لباس", "نوم"]):
        return 1
    if any(w in hay for w in ["وضوء", "أذان", "ذكر", "دعاء", "سواك", "رحم", "ضيف"]):
        return 2
    if any(w in hay for w in ["صلاة", "صيام", "قرآن", "علم", "حج", "عمرة", "صدقة"]):
        return 3
    if any(w in hay for w in ["قيام", "اعتكاف", "زكاة", "هجرة", "جهاد"]):
        return 4
    return 3

def minutes_for(level):
    return {1: 2, 2: 4, 3: 8, 4: 15, 5: 25}[level]

def is_prophetic_action_or_guidance(text):
    hay = normalize(text)

    prophetic_markers = [
        "قال رسول الله",
        "قال النبي",
        "عن النبي",
        "عن رسول الله",
        "أن النبي",
        "أن رسول الله",
        "كان رسول الله",
        "كان النبي",
        "رأيت رسول الله",
        "رأيت النبي",
        "أمر رسول الله",
        "أمر النبي",
        "نهى رسول الله",
        "نهى النبي",
        "فعل رسول الله",
        "فعل النبي",
        "سنة رسول الله",
        "هدي رسول الله",
    ]

    action_or_guidance_markers = [
        "أمر",
        "نهى",
        "قال",
        "كان",
        "إذا",
        "من كان",
        "من فعل",
        "افعلوا",
        "لا تفعلوا",
        "صلوا",
        "صوموا",
        "اذكروا",
        "قولوا",
        "كلوا",
        "اشربوا",
        "ناموا",
        "لا",
        "سن",
        "أحب",
        "كره",
    ]

    return (
        len(hay) >= 40
        and any(marker in hay for marker in prophetic_markers)
        and any(marker in hay for marker in action_or_guidance_markers)
    )

def load_candidates():
    result = []
    seen = set()

    for collection, directory, arabic_name, expected_book_id in SOURCE_DIRS:
        files = sorted(directory.glob("*.json"), key=lambda p: int(p.stem))
        if not files:
            raise SystemExit(f"No chapter files found for {arabic_name}: {directory}")

        for path in files:
            chapter_id = int(path.stem)
            data = json.loads(path.read_text(encoding="utf-8"))

            if not isinstance(data.get("hadiths"), list):
                raise SystemExit(f"Invalid chapter format: {path}")

            for hadith in data["hadiths"]:
                arabic = (hadith.get("arabic") or "").strip()
                if not is_prophetic_action_or_guidance(arabic):
                    continue

                raw_id = int(hadith.get("id", 0))
                hadith_number = int(hadith.get("idInBook", raw_id))
                chapter_from_row = int(hadith.get("chapterId", chapter_id))
                book_id = int(hadith.get("bookId", expected_book_id))

                if raw_id <= 0 or hadith_number <= 0 or chapter_from_row != chapter_id:
                    continue
                if book_id != expected_book_id:
                    raise SystemExit(
                        f"Source integrity error in {path}: expected bookId {expected_book_id}, got {book_id}"
                    )

                key = (collection, raw_id, normalize(arabic))
                if key in seen:
                    continue
                seen.add(key)

                result.append({
                    "collection": collection,
                    "book": arabic_name,
                    "chapter": f"الباب رقم {chapter_id}",
                    "hadith_number": hadith_number,
                    "raw_id": raw_id,
                    "chapter_id": chapter_id,
                    "book_id": book_id,
                    "arabic": arabic,
                    "source": f"{arabic_name} — حديث رقم {hadith_number} — الباب رقم {chapter_id}",
                })

    # Deterministic source order first; final user-facing order is by difficulty.
    result.sort(key=lambda x: (x["collection"], x["chapter_id"], x["raw_id"]))
    return result

def main():
    if not DB.exists():
        raise SystemExit(f"Missing database: {DB}")

    con = sqlite3.connect(DB)
    cur = con.cursor()

    existing_count = cur.execute("SELECT COUNT(*) FROM Sunnah").fetchone()[0]
    if existing_count > TARGET_TOTAL:
        con.close()
        raise SystemExit(f"Unexpected existing catalogue count: {existing_count}")

    if existing_count == TARGET_TOTAL:
        print(f"Already expanded to target: {existing_count}")
        con.close()
        return

    existing_texts = {normalize(r[0]) for r in cur.execute("SELECT arabicText FROM Hadith")}
    max_hadith_id = cur.execute("SELECT COALESCE(MAX(id), 0) FROM Hadith").fetchone()[0]
    max_sunnah_id = cur.execute("SELECT COALESCE(MAX(id), 0) FROM Sunnah").fetchone()[0]

    candidates = [
        item for item in load_candidates()
        if normalize(item["arabic"]) not in existing_texts
    ]

    needed = TARGET_TOTAL - existing_count
    if len(candidates) < needed:
        raise SystemExit(
            f"Only {len(candidates)} source-verified unique narrations found; {needed} required."
        )

    selected = candidates[:needed]
    selected.sort(
        key=lambda item: (
            difficulty_for(item["arabic"]),
            0 if item["collection"] == "SAHIH_BUKHARI" else 1,
            item["chapter_id"],
            item["raw_id"],
        )
    )

    hadith_rows = []
    sunnah_rows = []

    for offset, item in enumerate(selected, start=1):
        hid = max_hadith_id + offset
        sid = max_sunnah_id + offset
        level = difficulty_for(item["arabic"])
        category = category_for(item["arabic"])

        title = f"هدي نبوي موثّق — {category}"
        description = (
            f"النص المعروض مأخوذ مباشرة من {item['book']} في المرجع المذكور. "
            "لا يضيف التطبيق حكمًا شرعيًا جديدًا ولا ينسب للنبي ﷺ نصًا غير الوارد في المصدر."
        )

        hadith_rows.append((
            hid,
            item["collection"],
            item["book"],
            item["chapter"],
            item["hadith_number"],
            "راوي الحديث",
            item["arabic"],
            item["source"],
            "صحيح",
            0,
            None,
            item["raw_id"],
            item["chapter_id"],
            item["book_id"],
        ))

        sunnah_rows.append((
            sid,
            title,
            description,
            hid,
            level,
            category,
            minutes_for(level),
            sid,
            1,
        ))

    cur.executemany(
        """
        INSERT INTO Hadith (
            id, collection, book, chapter, hadithNumber, narrator, arabicText,
            sourceReference, authenticity, isAgreedUpon, linkedHadithId,
            rawId, chapterId, bookId
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        hadith_rows,
    )
    cur.executemany(
        """
        INSERT INTO Sunnah (
            id, title, description, hadithId, difficulty, category,
            estimatedMinutes, orderIndex, isActive
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        sunnah_rows,
    )

    cur.execute("INSERT INTO HadithFts(HadithFts) VALUES('rebuild')")
    con.commit()

    # Verify every newly inserted row still points to one of the two allowed Sahih collections
    # and has a complete source reference plus source identifiers.
    bad_rows = cur.execute(
        """
        SELECT COUNT(*)
        FROM Hadith
        WHERE id > ?
          AND (
              collection NOT IN ('SAHIH_BUKHARI', 'SAHIH_MUSLIM')
              OR book NOT IN ('صحيح البخاري', 'صحيح مسلم')
              OR sourceReference IS NULL OR TRIM(sourceReference) = ''
              OR rawId <= 0 OR chapterId <= 0 OR bookId NOT IN (1, 2)
              OR authenticity != 'صحيح'
          )
        """,
        (max_hadith_id,),
    ).fetchone()[0]

    if bad_rows:
        con.rollback()
        con.close()
        raise SystemExit(f"Source verification failed for {bad_rows} newly inserted hadith rows.")

    total = cur.execute("SELECT COUNT(*) FROM Sunnah").fetchone()[0]
    hadith_total = cur.execute("SELECT COUNT(*) FROM Hadith").fetchone()[0]
    integrity = cur.execute("PRAGMA integrity_check").fetchone()[0]
    distribution = cur.execute(
        "SELECT difficulty, COUNT(*) FROM Sunnah GROUP BY difficulty ORDER BY difficulty"
    ).fetchall()

    print(f"Expanded Sunnahs: {total}")
    print(f"Hadith rows: {hadith_total}")
    print(f"New verified rows: {len(selected)}")
    print(f"Source distribution: {[(c, sum(1 for x in selected if x['collection'] == c)) for c, _, _, _ in SOURCE_DIRS]}")
    print(f"Difficulty distribution: {distribution}")
    print(f"SQLite integrity: {integrity}")

    con.close()

if __name__ == "__main__":
    main()
