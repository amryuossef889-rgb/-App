package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Sunnah")
data class Sunnah(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val hadithId: Int,
    val difficulty: Int, // 1 to 5
    val category: String,
    val estimatedMinutes: Int,
    val orderIndex: Int,
    val isActive: Boolean = true
)
