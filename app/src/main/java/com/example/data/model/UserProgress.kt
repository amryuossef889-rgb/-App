package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserProgress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val currentSunnahId: Int = 1,
    val completedSunnahs: String = "[]", // JSON array of completed Sunnah IDs
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null, // yyyy-MM-dd
    val startedDate: String
)
