package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.Hadith
import com.example.data.model.PdfBook
import com.example.data.model.SunnahWithHadith
import com.example.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SunnahRepository(private val db: AppDatabase) {
    private val hadithDao = db.hadithDao()
    private val sunnahDao = db.sunnahDao()
    private val userProgressDao = db.userProgressDao()
    private val pdfBookDao = db.pdfBookDao()

    // Sunnahs
    fun getAllSunnahsWithHadith(): Flow<List<SunnahWithHadith>> = sunnahDao.getAllSunnahsWithHadith()

    fun getSunnahWithHadithById(id: Int): Flow<SunnahWithHadith?> = sunnahDao.getSunnahWithHadithById(id)

    suspend fun getSunnahWithHadithDirect(id: Int): SunnahWithHadith? = sunnahDao.getSunnahWithHadithDirect(id)

    fun getSunnahsByDifficulty(difficulty: Int): Flow<List<SunnahWithHadith>> = sunnahDao.getSunnahsByDifficulty(difficulty)

    fun getSunnahsByCategory(category: String): Flow<List<SunnahWithHadith>> = sunnahDao.getSunnahsByCategory(category)

    fun getAllCategories(): Flow<List<String>> = sunnahDao.getAllCategories()

    fun getSunnahsCount(): Flow<Int> = sunnahDao.getSunnahsCount()

    // Hadiths
    fun getHadithById(id: Int): Flow<Hadith?> = hadithDao.getHadithById(id)

    fun getHadithsCount(): Flow<Int> = hadithDao.getHadithsCount()

    fun searchHadiths(query: String, collection: String? = null): Flow<List<Hadith>> {
        val trimmed = query.trim()
        return if (collection.isNullOrBlank() || collection == "ALL") {
            hadithDao.searchHadiths(trimmed)
        } else {
            hadithDao.searchHadithsByCollection(collection, trimmed)
        }
    }

    // User Progress
    fun getUserProgress(): Flow<UserProgress?> = userProgressDao.getUserProgress()

    suspend fun markSunnahCompleted(sunnahId: Int): UserProgress {
        val currentProgress = userProgressDao.getUserProgressDirect() ?: UserProgress(
            id = 1,
            currentSunnahId = 1,
            completedSunnahs = "[]",
            currentStreak = 0,
            longestStreak = 0,
            lastCompletedDate = null,
            startedDate = getTodayDateString()
        )

        val completedSet = parseCompletedSunnahIds(currentProgress.completedSunnahs).toMutableSet()
        completedSet.add(sunnahId)

        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        val newStreak: Int
        if (currentProgress.lastCompletedDate == today) {
            // Already completed something today, streak maintains
            newStreak = if (currentProgress.currentStreak == 0) 1 else currentProgress.currentStreak
        } else if (currentProgress.lastCompletedDate == yesterday) {
            // Consecutive day
            newStreak = currentProgress.currentStreak + 1
        } else {
            // Gap or first time
            newStreak = 1
        }

        val newLongestStreak = maxOf(currentProgress.longestStreak, newStreak)

        // Find next uncompleted Sunnah ID in order 1..100
        var nextId = sunnahId + 1
        while (nextId <= 100 && completedSet.contains(nextId)) {
            nextId++
        }
        if (nextId > 100) {
            // Find first uncompleted from 1
            nextId = (1..100).firstOrNull { !completedSet.contains(it) } ?: 100
        }

        val jsonArray = JSONArray()
        completedSet.sorted().forEach { jsonArray.put(it) }

        val updatedProgress = currentProgress.copy(
            currentSunnahId = nextId,
            completedSunnahs = jsonArray.toString(),
            currentStreak = newStreak,
            longestStreak = newLongestStreak,
            lastCompletedDate = today
        )

        userProgressDao.insertOrUpdate(updatedProgress)
        return updatedProgress
    }

    suspend fun toggleSunnahCompletion(sunnahId: Int): Boolean {
        val currentProgress = userProgressDao.getUserProgressDirect() ?: return false
        val completedSet = parseCompletedSunnahIds(currentProgress.completedSunnahs).toMutableSet()
        val isNowCompleted = if (completedSet.contains(sunnahId)) {
            completedSet.remove(sunnahId)
            val jsonArray = JSONArray()
            completedSet.sorted().forEach { jsonArray.put(it) }
            userProgressDao.insertOrUpdate(currentProgress.copy(completedSunnahs = jsonArray.toString()))
            false
        } else {
            markSunnahCompleted(sunnahId)
            true
        }
        return isNowCompleted
    }

    // PDF Library
    fun getAllPdfBooks(): Flow<List<PdfBook>> = pdfBookDao.getAllBooks()

    fun getPdfBookById(id: Int): Flow<PdfBook?> = pdfBookDao.getBookById(id)

    suspend fun insertPdfBook(book: PdfBook): Long = pdfBookDao.insertBook(book)

    suspend fun updatePdfBook(book: PdfBook) = pdfBookDao.updateBook(book)

    suspend fun deletePdfBook(id: Int) = pdfBookDao.deleteBook(id)

    // Helper functions
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(calendar.time)
    }

    companion object {
        fun parseCompletedSunnahIds(jsonString: String): Set<Int> {
            val set = mutableSetOf<Int>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    set.add(array.getInt(i))
                }
            } catch (e: Exception) {
                // Return empty if parse failed
            }
            return set
        }
    }
}
