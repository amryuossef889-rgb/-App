package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class SunnahWithHadith(
    @Embedded val sunnah: Sunnah,
    @Relation(
        parentColumn = "hadithId",
        entityColumn = "id"
    )
    val hadith: Hadith?
)
