# مصادر بيانات الأحاديث (Hadith Data Sources)

تم الحصول على بيانات الأحاديث النبوية الشريفة من المستودع الموثوق والمفتوح:

- **اسم المصدر:** hadith-json (by AhmedBaset)
- **المستودع:** https://github.com/AhmedBaset/hadith-json
- **الإصدار / التاج الثابت:** `v1.2.0`
- **الترخيص:** MIT License
- **تاريخ الحصول على البيانات:** 2026-08-28

## الملفات المستخدمة وروابط الـ Raw:

1. **صحيح البخاري:**
   - الرابط: `https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_book/the_9_books/bukhari.json`
   - عدد الأحاديث: 7,277 حديث
   - عدد الكتب/الأبواب: 97 كتاباً

2. **صحيح مسلم:**
   - الرابط: `https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_book/the_9_books/muslim.json`
   - عدد الأحاديث: 7,459 حديث
   - عدد الكتب/الأبواب: 57 كتاباً

## مواصفات بنية البيانات:
```typescript
interface Hadith {
  id: number;
  chapterId: number;
  bookId: number;
  arabic: string;
  english: {
    narrator: string;
    text: string;
  };
}
```
جميع النصوص العربية تم الاحتفاظ بها كاملة دون أي تعديل أو تحريف أو نقص، مع الحفاظ على المعرفات الأصلية للتحقق والتتبع.
