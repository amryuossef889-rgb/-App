package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM UserProgress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Query("SELECT * FROM UserProgress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: UserProgress)
}
