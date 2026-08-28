package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PdfBook")
data class PdfBook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val filename: String,
    val size: Long,
    val addedDate: Long,
    val isBuiltin: Boolean = false
)
