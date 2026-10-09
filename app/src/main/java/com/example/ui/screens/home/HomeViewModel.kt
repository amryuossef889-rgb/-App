package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.SunnahWithHadith
import com.example.data.model.UserProgress
import com.example.data.repository.SunnahRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class HomeUiState(
    val currentSunnahWithHadith: SunnahWithHadith? = null,
    val userProgress: UserProgress? = null,
    val completedSunnahIds: Set<Int> = emptySet(),
    val totalSunnahsCount: Int = 0,
    val upcomingSunnahs: List<SunnahWithHadith> = emptyList(),
    val isTodayCompleted: Boolean = false,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val sunnahRepository: SunnahRepository
) : ViewModel() {

    private val _userProgress = sunnahRepository.getUserProgress()
    private val _allSunnahs = sunnahRepository.getAllSunnahsWithHadith()

    val uiState: StateFlow<HomeUiState> = combine(
        _userProgress,
        _allSunnahs
    ) { progress, sunnahs ->
        val allSunnahIds = sunnahs.mapTo(mutableSetOf()) { it.sunnah.id }
        val completedSet = SunnahRepository.parseCompletedSunnahIds(progress?.completedSunnahs ?: "[]")
            .intersect(allSunnahIds)

        // The learning path is always easiest-first. Do not use database insertion order:
        // older rows can have IDs/orderIndex values unrelated to their difficulty.
        val learningPath = sunnahs.sortedWith(
            compareBy<SunnahWithHadith> { it.sunnah.difficulty }
                .thenBy { it.sunnah.orderIndex }
                .thenBy { it.sunnah.id }
        )

        // Day 1 (the start date) shows the easiest item. Each following calendar day
        // advances exactly one item along the easy-to-hard path, and then stays at the
        // hardest item once the catalogue is exhausted. Invalid/missing dates start at day 1.
        val dayIndex = dailyIndex(progress?.startedDate, todayDate(), learningPath.size)
        val currentSunnah = learningPath.getOrNull(dayIndex)
            ?: learningPath.firstOrNull()

        val upcoming = if (currentSunnah == null) {
            emptyList()
        } else {
            val currentIndex = learningPath.indexOfFirst { it.sunnah.id == currentSunnah.sunnah.id }
            learningPath.drop(currentIndex + 1)
                .filter { !completedSet.contains(it.sunnah.id) }
                .take(3)
        }

        val currentId = currentSunnah?.sunnah?.id
        HomeUiState(
            currentSunnahWithHadith = currentSunnah,
            userProgress = progress,
            completedSunnahIds = completedSet,
            totalSunnahsCount = sunnahs.size,
            upcomingSunnahs = upcoming,
            isTodayCompleted = currentId != null && completedSet.contains(currentId),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleCurrentSunnah() {
        val currentId = uiState.value.currentSunnahWithHadith?.sunnah?.id ?: return
        viewModelScope.launch {
            sunnahRepository.toggleSunnahCompletion(currentId)
        }
    }

    fun toggleSunnah(sunnahId: Int) {
        viewModelScope.launch {
            sunnahRepository.toggleSunnahCompletion(sunnahId)
        }
    }

    companion object {
        internal fun dailyIndex(startedDate: String?, today: String, itemCount: Int): Int {
            if (itemCount <= 1) return 0
            if (startedDate.isNullOrBlank()) return 0

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return try {
                val start = formatter.parse(startedDate) ?: return 0
                val current = formatter.parse(today) ?: return 0
                val days = ((current.time - start.time) / MILLIS_PER_DAY).toInt()
                days.coerceIn(0, itemCount - 1)
            } catch (_: Exception) {
                0
            }
        }

        private fun todayDate(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.format(Calendar.getInstance().time)
        }

        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

        fun provideFactory(sunnahRepository: SunnahRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(sunnahRepository) as T
                }
            }
    }
}
