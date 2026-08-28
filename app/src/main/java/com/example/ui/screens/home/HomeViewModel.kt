package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.SunnahWithHadith
import com.example.data.model.UserProgress
import com.example.data.repository.SunnahRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentSunnahWithHadith: SunnahWithHadith? = null,
    val userProgress: UserProgress? = null,
    val completedSunnahIds: Set<Int> = emptySet(),
    val totalSunnahsCount: Int = 100,
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
        val currentId = progress?.currentSunnahId ?: 1
        val completedSet = SunnahRepository.parseCompletedSunnahIds(progress?.completedSunnahs ?: "[]")
        val currentSunnah = sunnahs.find { it.sunnah.id == currentId } ?: sunnahs.firstOrNull()

        // Upcoming uncompleted sunnahs
        val upcoming = sunnahs.filter { !completedSet.contains(it.sunnah.id) && it.sunnah.id != currentId }.take(3)

        val isCompleted = completedSet.contains(currentId)

        HomeUiState(
            currentSunnahWithHadith = currentSunnah,
            userProgress = progress,
            completedSunnahIds = completedSet,
            totalSunnahsCount = sunnahs.size.takeIf { it > 0 } ?: 100,
            upcomingSunnahs = upcoming,
            isTodayCompleted = isCompleted,
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
        fun provideFactory(sunnahRepository: SunnahRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(sunnahRepository) as T
                }
            }
    }
}
