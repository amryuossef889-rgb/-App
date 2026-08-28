package com.example.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.Sunnah
import com.example.data.model.SunnahWithHadith
import kotlinx.coroutines.flow.Flow

@Dao
interface SunnahDao {
    @Query("SELECT * FROM Sunnah ORDER BY orderIndex ASC")
    fun getAllSunnahs(): Flow<List<Sunnah>>

    @Transaction
    @Query("SELECT * FROM Sunnah ORDER BY orderIndex ASC")
    fun getAllSunnahsWithHadith(): Flow<List<SunnahWithHadith>>

    @Transaction
    @Query("SELECT * FROM Sunnah WHERE id = :id LIMIT 1")
    fun getSunnahWithHadithById(id: Int): Flow<SunnahWithHadith?>

    @Transaction
    @Query("SELECT * FROM Sunnah WHERE id = :id LIMIT 1")
    suspend fun getSunnahWithHadithDirect(id: Int): SunnahWithHadith?

    @Transaction
    @Query("SELECT * FROM Sunnah WHERE difficulty = :difficulty ORDER BY orderIndex ASC")
    fun getSunnahsByDifficulty(difficulty: Int): Flow<List<SunnahWithHadith>>

    @Transaction
    @Query("SELECT * FROM Sunnah WHERE category = :category ORDER BY orderIndex ASC")
    fun getSunnahsByCategory(category: String): Flow<List<SunnahWithHadith>>

    @Query("SELECT COUNT(*) FROM Sunnah")
    fun getSunnahsCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM Sunnah ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}
