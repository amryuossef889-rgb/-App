import re
import sqlite3
from collections import defaultdict
from pathlib import Path

DB = Path('app/src/main/assets/databases/sunnah.db')

def normalize(text):
    text = re.sub(r'[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]', '', text or '')
    text = re.sub(r'[إأآٱ]', 'ا', text)
    text = text.replace('ى', 'ي').replace('ـ', '')
    return re.sub(r'\\s+', ' ', text).strip()

con = sqlite3.connect(DB)
rows = con.execute('SELECT s.id, h.collection, h.rawId, h.arabicText FROM Sunnah s JOIN Hadith h ON h.id=s.hadithId WHERE s.isActive=1').fetchall()
if len(rows) != 1100:
    raise SystemExit(f'Expected 1100 active linked hadiths, found {len(rows)}')
texts, sources = defaultdict(list), defaultdict(list)
for sid, collection, raw_id, arabic in rows:
    texts[normalize(arabic)].append(sid)
    sources[(collection, raw_id)].append(sid)
duplicates = {k:v for k,v in texts.items() if k and len(v)>1}
source_duplicates = {k:v for k,v in sources.items() if len(v)>1}
empty = [sid for sid, collection, raw_id, arabic in rows if not normalize(arabic)]
print(f'Active hadiths: {len(rows)}; unique normalized texts: {len(texts)}')
print(f'Duplicate normalized text groups: {len(duplicates)}; duplicate source keys: {len(source_duplicates)}; empty texts: {len(empty)}')
if duplicates or source_duplicates or empty:
    raise SystemExit('Hadith duplicate audit failed')
print('Hadith duplicate audit: OK')
