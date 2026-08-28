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
        WHERE arabicText LIKE '%' || :query || '%' 
           OR narrator LIKE '%' || :query || '%' 
           OR book LIKE '%' || :query || '%' 
           OR sourceReference LIKE '%' || :query || '%'
           OR CAST(hadithNumber AS TEXT) = :query
        ORDER BY id ASC 
        LIMIT 100
    """)
    fun searchHadiths(query: String): Flow<List<Hadith>>

    @Query("""
        SELECT * FROM Hadith 
        WHERE collection = :collection 
          AND (arabicText LIKE '%' || :query || '%' 
               OR narrator LIKE '%' || :query || '%' 
               OR book LIKE '%' || :query || '%'
               OR CAST(hadithNumber AS TEXT) = :query)
        ORDER BY id ASC 
        LIMIT 100
    """)
    fun searchHadithsByCollection(collection: String, query: String): Flow<List<Hadith>>

    @Query("SELECT COUNT(*) FROM Hadith")
    fun getHadithsCount(): Flow<Int>

    @Query("SELECT * FROM Hadith WHERE isAgreedUpon = 1 LIMIT 50")
    fun getAgreedUponHadiths(): Flow<List<Hadith>>
}
