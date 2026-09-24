package com.example.data.database

import androidx.room.Dao
import androidx.room.Query
import com.example.data.model.Hadith
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {
    @Query("SELECT * FROM Hadith WHERE id = :id LIMIT 1")
    fun getHadithById(id: Int): Flow<Hadith?>

    @Query("SELECT * FROM Hadith WHERE id = :id LIMIT 1")
    suspend fun getHadithDirect(id: Int): Hadith?

    @Query("""
        SELECT * FROM Hadith
        WHERE LENGTH(TRIM(:query)) > 0
          AND (
              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(arabicText, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR
              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(narrator, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR
              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(book, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR
              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(chapter, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR
              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sourceReference, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR CAST(hadithNumber AS TEXT) LIKE '%' || TRIM(:query) || '%'
          )
        ORDER BY id ASC
    """)
    fun searchHadiths(query: String): Flow<List<Hadith>>

    @Query("""
        SELECT * FROM Hadith
        WHERE collection = :collection
          AND LENGTH(TRIM(:query)) > 0
          AND (
              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(arabicText, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(narrator, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(book, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(chapter, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR              REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(sourceReference, 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') LIKE '%' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(:query), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'), 'ٱ', 'ا'), 'ة', 'ه'), 'ى', 'ي'), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'َ', ''), 'ُ', ''), 'ِ', ''), 'ّ', ''), 'ْ', ''), 'ً', ''), 'ٌ', ''), 'ٍ', ''), 'ـ', '') || '%'
              OR CAST(hadithNumber AS TEXT) LIKE '%' || TRIM(:query) || '%'
          )
        ORDER BY id ASC
    """)
    fun searchHadithsByCollection(collection: String, query: String): Flow<List<Hadith>>

    @Query("SELECT COUNT(*) FROM Hadith")
    fun getHadithsCount(): Flow<Int>

    @Query("SELECT * FROM Hadith WHERE isAgreedUpon = 1 LIMIT 50")
    fun getAgreedUponHadiths(): Flow<List<Hadith>>
}
