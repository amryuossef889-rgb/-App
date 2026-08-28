#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import sqlite3
import os
import re

def normalize_arabic(text):
    if not text:
        return ""
    # Remove tashkeel/diacritics for easier search matching
    text = re.sub(r'[\u064B-\u065F\u0670\u0640]', '', text)
    text = re.sub(r'[إأآا]', 'ا', text)
    text = re.sub(r'ى', 'ي', text)
    text = re.sub(r'ة', 'ه', text)
    return text

def main():
    print("Reading Bukhari and Muslim raw data...")
    with open("data/raw/bukhari.json", "r", encoding="utf-8") as f:
        bukhari_data = json.load(f)
    with open("data/raw/muslim.json", "r", encoding="utf-8") as f:
        muslim_data = json.load(f)

    bukhari_chapters = {c['id']: c['arabic'] for c in bukhari_data.get('chapters', [])}
    muslim_chapters = {c['id']: c['arabic'] for c in muslim_data.get('chapters', [])}

    db_dir = "app/src/main/assets/databases"
    os.makedirs(db_dir, exist_ok=True)
    db_path = os.path.join(db_dir, "sunnah.db")
    if os.path.exists(db_path):
        os.remove(db_path)

    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    # Create tables
    cur.execute("""
    CREATE TABLE IF NOT EXISTS Hadith (
        id INTEGER PRIMARY KEY,
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
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS Sunnah (
        id INTEGER PRIMARY KEY,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        hadithId INTEGER NOT NULL,
        difficulty INTEGER NOT NULL,
        category TEXT NOT NULL,
        estimatedMinutes INTEGER NOT NULL,
        orderIndex INTEGER NOT NULL,
        isActive INTEGER NOT NULL
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS UserProgress (
        id INTEGER PRIMARY KEY,
        currentSunnahId INTEGER NOT NULL,
        completedSunnahs TEXT NOT NULL,
        currentStreak INTEGER NOT NULL,
        longestStreak INTEGER NOT NULL,
        lastCompletedDate TEXT,
        startedDate TEXT NOT NULL
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS PdfBook (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        filename TEXT NOT NULL,
        size INTEGER NOT NULL,
        addedDate INTEGER NOT NULL,
        isBuiltin INTEGER NOT NULL
    )
    """)

    cur.execute("""
    CREATE VIRTUAL TABLE IF NOT EXISTS HadithFts USING fts4(
        content="Hadith",
        arabicText,
        narrator,
        book,
        chapter,
        sourceReference
    )
    """)

    print("Inserting Bukhari hadiths...")
    bukhari_hadiths = bukhari_data.get('hadiths', [])
    for h in bukhari_hadiths:
        hid = h['id']
        chapter_name = bukhari_chapters.get(h['chapterId'], "صحيح البخاري")
        narrator = h.get('english', {}).get('narrator', '')
        # Clean english narrator if present
        narrator_str = narrator if narrator else "عن الصحابي رضي الله عنه"
        arabic_text = h.get('arabic', '')
        source_ref = f"صحيح البخاري - {chapter_name} (حديث رقم {h.get('idInBook', hid)})"
        
        cur.execute("""
        INSERT INTO Hadith (
            id, collection, book, chapter, hadithNumber, narrator,
            arabicText, sourceReference, authenticity, isAgreedUpon,
            linkedHadithId, rawId, chapterId, bookId
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            hid, "SAHIH_BUKHARI", chapter_name, chapter_name, h.get('idInBook', hid),
            narrator_str, arabic_text, source_ref, "صحيح", 0, None,
            hid, h['chapterId'], h['bookId']
        ))

    print("Inserting Muslim hadiths...")
    muslim_hadiths = muslim_data.get('hadiths', [])
    for h in muslim_hadiths:
        hid = h['id']
        chapter_name = muslim_chapters.get(h['chapterId'], "صحيح مسلم")
        narrator = h.get('english', {}).get('narrator', '')
        narrator_str = narrator if narrator else "عن الصحابي رضي الله عنه"
        arabic_text = h.get('arabic', '')
        source_ref = f"صحيح مسلم - {chapter_name} (حديث رقم {h.get('idInBook', hid)})"
        
        cur.execute("""
        INSERT INTO Hadith (
            id, collection, book, chapter, hadithNumber, narrator,
            arabicText, sourceReference, authenticity, isAgreedUpon,
            linkedHadithId, rawId, chapterId, bookId
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            hid, "SAHIH_MUSLIM", chapter_name, chapter_name, h.get('idInBook', hid),
            narrator_str, arabic_text, source_ref, "صحيح", 0, None,
            hid, h['chapterId'], h['bookId']
        ))

    # Mark well-known verified agreed-upon hadiths
    # 1. Hadith 1 in Bukhari (إنما الأعمال بالنيات)
    # Let's find corresponding in Muslim and link them
    cur.execute("SELECT id FROM Hadith WHERE collection='SAHIH_MUSLIM' AND arabicText LIKE '%إنما الأعمال بالنيات%' LIMIT 1")
    row = cur.fetchone()
    if row:
        muslim_niyyah_id = row[0]
        cur.execute("UPDATE Hadith SET isAgreedUpon = 1, linkedHadithId = ? WHERE id = 1", (muslim_niyyah_id,))
        cur.execute("UPDATE Hadith SET isAgreedUpon = 1, linkedHadithId = 1 WHERE id = ?", (muslim_niyyah_id,))

    # Populate FTS table
    print("Populating FTS index...")
    cur.execute("INSERT INTO HadithFts(docid, arabicText, narrator, book, chapter, sourceReference) SELECT id, arabicText, narrator, book, chapter, sourceReference FROM Hadith")

    # Initialize UserProgress
    cur.execute("""
    INSERT INTO UserProgress (id, currentSunnahId, completedSunnahs, currentStreak, longestStreak, lastCompletedDate, startedDate)
    VALUES (1, 1, '[]', 0, 0, NULL, date('now'))
    """)

    conn.commit()
    conn.close()
    print("Database base populated successfully!")

if __name__ == "__main__":
    main()
