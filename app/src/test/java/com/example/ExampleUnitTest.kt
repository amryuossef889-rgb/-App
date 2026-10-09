package com.example

import com.example.data.model.UserProgress
import com.example.data.repository.SunnahRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun currentStreakRemainsActiveOnCompletionDay() {
        val progress = UserProgress(currentStreak = 4, longestStreak = 4, lastCompletedDate = "2026-10-09", startedDate = "2026-10-01")
        assertEquals(4, SunnahRepository.effectiveCurrentStreak(progress, "2026-10-09", "2026-10-08"))
    }

    @Test
    fun currentStreakRemainsActiveTheDayAfterCompletion() {
        val progress = UserProgress(currentStreak = 4, longestStreak = 4, lastCompletedDate = "2026-10-08", startedDate = "2026-10-01")
        assertEquals(4, SunnahRepository.effectiveCurrentStreak(progress, "2026-10-09", "2026-10-08"))
    }

    @Test
    fun currentStreakExpiresAfterMissedDay() {
        val progress = UserProgress(currentStreak = 4, longestStreak = 4, lastCompletedDate = "2026-10-07", startedDate = "2026-10-01")
        assertEquals(0, SunnahRepository.effectiveCurrentStreak(progress, "2026-10-09", "2026-10-08"))
    }

    @Test
    fun missingProgressHasNoActiveStreak() {
        assertEquals(0, SunnahRepository.effectiveCurrentStreak(null, "2026-10-09", "2026-10-08"))
    }

    @Test
    fun additionIsCorrect() {
        assertEquals(4, 2 + 2)
    }
}
