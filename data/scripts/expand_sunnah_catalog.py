#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Canonicalize and expand the app catalogue using Sahih al-Bukhari and Sahih Muslim only.

Every Hadith row produced by this script is copied from a pinned source JSON record.
Existing Sunnah/Hadith IDs are preserved where possible so UserProgress completion IDs
remain stable. Existing hand-curated text is replaced by canonical source text to avoid
unverified references or added religious claims.
"""

import json
import re
import sqlite3
from pathlib import Path
from collections import defaultdict

DB = Path("app/src/main/assets/databases/sunnah.db")
TARGET_SUNNAHS = 1200
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

def is_prophetic_hadith(text):
    hay = normalize(text)
    prophetic_markers = [
        "قال رسول الله", "قال النبي", "عن النبي", "عن رسول الله",
        "ان النبي", "ان رسول الله", "كان رسول الله", "كان النبي",
        "رايت رسول الله", "رايت النبي", "امر رسول الله", "امر النبي",
        "نهى رسول الله", "نهى النبي", "فعل رسول الله", "فعل النبي",
        "سنة رسول الله", "هدي رسول الله", "قيل لرسول الله", "قيل للنبي",
        "سأل رسول الله", "سأل النبي", "اخبر رسول الله", "اخبر النبي",
    ]
    return len(hay) >= 40 and any(marker in hay for marker in prophetic_markers)

def load_sources():
    records = []
    seen = set()

    for collection, directory, arabic_name, expected_book_id in SOURCE_DIRS:
        files = sorted(directory.glob("*.json"), key=lambda p: int(p.stem))
        if not files:
            raise SystemExit(f"No source chapter files found: {directory}")

        for path in files:
            chapter_id = int(path.stem)
            data = json.loads(path.read_text(encoding="utf-8"))
            hadiths = data.get("hadiths")
            if not isinstance(hadiths, list):
                raise SystemExit(f"Invalid source format: {path}")

            for hadith in hadiths:
                arabic = (hadith.get("arabic") or "").strip()
                if not is_prophetic_hadith(arabic):
                    continue

                raw_id = int(hadith.get("id", 0))
                hadith_number = int(hadith.get("idInBook", raw_id))
                row_chapter = int(hadith.get("chapterId", chapter_id))
                book_id = int(hadith.get("bookId", expected_book_id))

                if raw_id <= 0 or hadith_number <= 0 or row_chapter != chapter_id:
                    continue
                if book_id != expected_book_id:
                    raise SystemExit(
                        f"Source mismatch in {path}: expected bookId={expected_book_id}, got {book_id}"
                    )

                key = (collection, raw_id)
                if key in seen:
                    continue
                seen.add(key)

                records.append({
                    "key": key,
                    "collection": collection,
                    "book": arabic_name,
                    "chapter": f"الباب رقم {chapter_id}",
                    "hadith_number": hadith_number,
                    "raw_id": raw_id,
                    "chapter_id": chapter_id,
                    "book_id": book_id,
                    "arabic": arabic,
                    "normalized": normalize(arabic),
                    "source": f"{arabic_name} — حديث رقم {hadith_number} — الباب رقم {chapter_id}",
                })

    records.sort(
        key=lambda r: (
            difficulty_for(r["arabic"]),
            0 if r["collection"] == "SAHIH_BUKHARI" else 1,
            r["chapter_id"],
            r["raw_id"],
        )
    )

    if len(records) < TARGET_SUNNAHS:
        raise SystemExit(
            f"Only {len(records)} usable source-verified narrations found; {TARGET_SUNNAHS} required."
        )

    return records

def best_source_match(row, sources, by_claimed_key, exact_map, used):
    row_text = normalize(row["arabicText"])
    if not row_text:
        return None

    # First honor the existing claimed collection/number only when its text is actually
    # contained in the pinned source record.
    claimed_key = (
        row["collection"],
        int(row["rawId"] or 0),
    )
    claimed = by_claimed_key.get(claimed_key)
    if claimed and claimed["key"] not in used:
        source_text = claimed["normalized"]
        if row_text == source_text or (len(row_text) >= 35 and row_text in source_text):
            return claimed

    # Exact normalized text is the strongest repair match.
    for candidate in exact_map.get(row_text, []):
        if candidate["key"] not in used:
            return candidate

    # Existing hand-curated rows often contain the matn without the full isnad.
    # Use a long substring only to repair those rows, never to create new rows.
    if len(row_text) >= 70:
        best = None
        best_len = 0
        for candidate in sources:
            if candidate["key"] in used:
                continue
            text = candidate["normalized"]
            if row_text in text and len(row_text) > best_len:
                best = candidate
                best_len = len(row_text)
        if best:
            return best

    return None

def update_hadith(cur, hadith_id, source):
    cur.execute(
        """
        UPDATE Hadith
        SET collection=?, book=?, chapter=?, hadithNumber=?, narrator=?,
            arabicText=?, sourceReference=?, authenticity=?, isAgreedUpon=?,
            linkedHadithId=NULL, rawId=?, chapterId=?, bookId=?
        WHERE id=?
        """,
        (
            source["collection"], source["book"], source["chapter"], source["hadith_number"],
            "راوي الحديث", source["arabic"], source["source"], "صحيح", 0,
            source["raw_id"], source["chapter_id"], source["book_id"], hadith_id,
        ),
    )

def make_sunnah_values(sid, hadith_id, source):
    level = difficulty_for(source["arabic"])
    category = category_for(source["arabic"])
    return (
        sid,
        f"حديث نبوي موثّق — {category}",
        f"النص المعروض مأخوذ مباشرة من {source['book']} في المرجع المذكور. "
        "التصنيف حسب السهولة هنا تنظيمي داخل التطبيق فقط، ولا يمثل حكمًا على منزلة الحديث أو فضله.",
        hadith_id,
        level,
        category,
        {1: 2, 2: 4, 3: 8, 4: 15, 5: 25}[level],
        sid,
        1,
    )

def main():
    if not DB.exists():
        raise SystemExit(f"Missing database: {DB}")

    sources = load_sources()
    source_by_key = {s["key"]: s for s in sources}
    by_claimed_key = source_by_key
    exact_map = defaultdict(list)
    for source in sources:
        exact_map[source["normalized"]].append(source)

    con = sqlite3.connect(DB)
    cur = con.cursor()

    existing_sunnahs = cur.execute(
        "SELECT id, hadithId FROM Sunnah ORDER BY id ASC"
    ).fetchall()
    existing_hadiths = cur.execute(
        """
        SELECT id, collection, hadithNumber, arabicText, rawId, chapterId, bookId
        FROM Hadith
        ORDER BY id ASC
        """
    ).fetchall()

    hadith_rows = {
        row[0]: {
            "id": row[0],
            "collection": row[1],
            "hadithNumber": row[2],
            "arabicText": row[3],
            "rawId": row[4],
            "chapterId": row[5],
            "bookId": row[6],
        }
        for row in existing_hadiths
    }

    used = set()
    assignments = {}
    repaired = 0
    replaced = 0

    # Repair existing Sunnah-linked Hadith rows first so completion IDs stay stable.
    for sid, hid in existing_sunnahs:
        row = hadith_rows.get(hid)
        if not row:
            continue
        match = best_source_match(row, sources, by_claimed_key, exact_map, used)
        if match:
            assignments[hid] = match
            used.add(match["key"])
            repaired += 1

    # Any remaining existing Hadith rows are also canonicalized to a unique source hadith.
    for hid, row in hadith_rows.items():
        if hid in assignments:
            continue
        unused = next(
            (s for s in sources if s["key"] not in used),
            None,
        )
        if not unused:
            raise SystemExit("Source pool exhausted while repairing existing Hadith rows.")
        assignments[hid] = unused
        used.add(unused["key"])
        replaced += 1

    # Update all existing Hadith rows to canonical source text/references.
    for hid, source in assignments.items():
        update_hadith(cur, hid, source)

    # Existing Sunnah rows keep their IDs. Repoint them to their existing Hadith IDs,
    # and use a source-faithful generic title/description.
    for sid, hid in existing_sunnahs:
        source = assignments.get(hid)
        if not source:
            raise SystemExit(f"Sunnah {sid} points to missing Hadith {hid}.")
        cur.execute(
            """
            UPDATE Sunnah
            SET title=?, description=?, hadithId=?, difficulty=?, category=?,
                estimatedMinutes=?, orderIndex=?, isActive=1
            WHERE id=?
            """,
            make_sunnah_values(sid, hid, source)[:-1] + (sid,),
        )

    # Append source-verified rows until exactly TARGET_SUNNAHS Sunnahs exist.
    current_count = len(existing_sunnahs)
    max_sid = max((sid for sid, _ in existing_sunnahs), default=0)
    max_hid = max(hadith_rows.keys(), default=0)

    selected_new = []
    for source in sources:
        if source["key"] in used:
            continue
        if current_count + len(selected_new) >= TARGET_SUNNAHS:
            break
        selected_new.append(source)
        used.add(source["key"])

    if current_count + len(selected_new) < TARGET_SUNNAHS:
        raise SystemExit(
            f"Could only build {current_count + len(selected_new)} Sunnahs; target is {TARGET_SUNNAHS}."
        )

    for offset, source in enumerate(selected_new, start=1):
        hid = max_hid + offset
        sid = max_sid + offset
        update_values = (
            hid, source["collection"], source["book"], source["chapter"],
            source["hadith_number"], "راوي الحديث", source["arabic"],
            source["source"], "صحيح", 0, None,
            source["raw_id"], source["chapter_id"], source["book_id"],
        )
        cur.execute(
            """
            INSERT INTO Hadith (
                id, collection, book, chapter, hadithNumber, narrator, arabicText,
                sourceReference, authenticity, isAgreedUpon, linkedHadithId,
                rawId, chapterId, bookId
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            update_values,
        )
        cur.execute(
            """
            INSERT INTO Sunnah (
                id, title, description, hadithId, difficulty, category,
                estimatedMinutes, orderIndex, isActive
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            make_sunnah_values(sid, hid, source),
        )

    cur.execute("INSERT INTO HadithFts(HadithFts) VALUES('rebuild')")

    # Strict audit: every Sunnah-linked Hadith in the final DB must exactly match a pinned
    # Bukhari/Muslim source record after normalization, and every source reference must match.
    final_hadiths = cur.execute(
        """
        SELECT h.id, h.collection, h.hadithNumber, h.arabicText, h.sourceReference,
               h.rawId, h.chapterId, h.bookId, s.id
        FROM Hadith h
        JOIN Sunnah s ON s.hadithId = h.id
        ORDER BY s.id
        """
    ).fetchall()

    if len(final_hadiths) != TARGET_SUNNAHS:
        raise SystemExit(
            f"Final linked catalogue count mismatch: {len(final_hadiths)} != {TARGET_SUNNAHS}"
        )

    final_seen = set()
    for (
        hid, collection, hadith_number, arabic, source_reference,
        raw_id, chapter_id, book_id, sid
    ) in final_hadiths:
        key = (collection, int(raw_id))
        source = source_by_key.get(key)
        if not source:
            raise SystemExit(
                f"Verification failed: Sunnah {sid}/Hadith {hid} points outside Bukhari/Muslim: {key}"
            )
        if normalize(arabic) != source["normalized"]:
            raise SystemExit(
                f"Verification failed: Arabic text mismatch for Sunnah {sid}/Hadith {hid} source {key}"
            )
        if int(hadith_number) != source["hadith_number"]:
            raise SystemExit(
                f"Verification failed: hadith number mismatch for Sunnah {sid}/Hadith {hid}"
            )
        if int(chapter_id) != source["chapter_id"] or int(book_id) != source["book_id"]:
            raise SystemExit(
                f"Verification failed: chapter/book mismatch for Sunnah {sid}/Hadith {hid}"
            )
        if source_reference != source["source"]:
            raise SystemExit(
                f"Verification failed: source reference mismatch for Sunnah {sid}/Hadith {hid}"
            )
        if key in final_seen:
            raise SystemExit(f"Verification failed: duplicate source row used: {key}")
        final_seen.add(key)

    integrity = cur.execute("PRAGMA integrity_check").fetchone()[0]
    if integrity != "ok":
        raise SystemExit(f"SQLite integrity check failed: {integrity}")

    con.commit()

    distribution = cur.execute(
        "SELECT difficulty, COUNT(*) FROM Sunnah GROUP BY difficulty ORDER BY difficulty"
    ).fetchall()
    bukhari_count = cur.execute(
        "SELECT COUNT(*) FROM Hadith WHERE collection='SAHIH_BUKHARI'"
    ).fetchone()[0]
    muslim_count = cur.execute(
        "SELECT COUNT(*) FROM Hadith WHERE collection='SAHIH_MUSLIM'"
    ).fetchone()[0]

    print(f"Canonical verified Sunnahs: {TARGET_SUNNAHS}")
    print(f"Canonical verified Hadiths linked to Sunnahs: {len(final_hadiths)}")
    print(f"Existing rows repaired from source: {repaired}")
    print(f"Existing rows replaced with verified source records: {replaced}")
    print(f"Bukhari: {bukhari_count} | Muslim: {muslim_count}")
    print(f"Difficulty distribution: {distribution}")
    print("Strict source audit: OK")
    print(f"SQLite integrity: {integrity}")

    con.close()

if __name__ == "__main__":
    main()
