package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Hadith
import com.example.data.model.PdfBook
import com.example.data.model.Sunnah
import com.example.data.model.UserProgress

@Database(
    entities = [
        Hadith::class,
        Sunnah::class,
        UserProgress::class,
        PdfBook::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hadithDao(): HadithDao
    abstract fun sunnahDao(): SunnahDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun pdfBookDao(): PdfBookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sunnah.db"
                )
                .createFromAsset("databases/sunnah.db")
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
