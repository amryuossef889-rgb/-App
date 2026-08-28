package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Hadith")
data class Hadith(
    @PrimaryKey val id: Int,
    val collection: String,
    val book: String,
    val chapter: String,
    val hadithNumber: Int,
    val narrator: String,
    val arabicText: String,
    val sourceReference: String,
    val authenticity: String,
    val isAgreedUpon: Boolean,
    val linkedHadithId: Int?,
    val rawId: Int,
    val chapterId: Int,
    val bookId: Int
)
