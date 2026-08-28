package com.example.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.SunnahWithHadith
import com.example.data.repository.SunnahRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SunnahDetailUiState(
    val sunnahWithHadith: SunnahWithHadith? = null,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = true
)

class SunnahDetailViewModel(
    private val sunnahId: Int,
    private val sunnahRepository: SunnahRepository
) : ViewModel() {

    private val _sunnahFlow = sunnahRepository.getSunnahWithHadithById(sunnahId)
    private val _userProgressFlow = sunnahRepository.getUserProgress()

    val uiState: StateFlow<SunnahDetailUiState> = combine(
        _sunnahFlow,
        _userProgressFlow
    ) { sunnah, progress ->
        val completedSet = SunnahRepository.parseCompletedSunnahIds(progress?.completedSunnahs ?: "[]")
        SunnahDetailUiState(
            sunnahWithHadith = sunnah,
            isCompleted = completedSet.contains(sunnahId),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SunnahDetailUiState()
    )

    fun toggleCompletion() {
        viewModelScope.launch {
            sunnahRepository.toggleSunnahCompletion(sunnahId)
        }
    }

    companion object {
        fun provideFactory(sunnahId: Int, sunnahRepository: SunnahRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SunnahDetailViewModel(sunnahId, sunnahRepository) as T
                }
            }
    }
}
