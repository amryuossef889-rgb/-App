package com.example.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyDifficultyProgressionTest {
    @Test
    fun startDateShowsEasiestItemOnDayOne() {
        assertEquals(0, HomeViewModel.dailyIndex("2026-10-09", "2026-10-09", 1100))
    }

    @Test
    fun advancesOneItemPerCalendarDay() {
        assertEquals(1, HomeViewModel.dailyIndex("2026-10-09", "2026-10-10", 1100))
        assertEquals(2, HomeViewModel.dailyIndex("2026-10-09", "2026-10-11", 1100))
    }

    @Test
    fun progressionNeverWrapsBackToEasyAfterLastItem() {
        assertEquals(1099, HomeViewModel.dailyIndex("2020-01-01", "2026-10-09", 1100))
    }

    @Test
    fun missingOrInvalidStartDateFallsBackToEasiestItem() {
        assertEquals(0, HomeViewModel.dailyIndex(null, "2026-10-09", 1100))
        assertEquals(0, HomeViewModel.dailyIndex("not-a-date", "2026-10-09", 1100))
    }
}
