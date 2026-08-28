package com.example.ui.screens.sunnahs

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

enum class StatusFilter {
    ALL, UNCOMPLETED, COMPLETED
}

private data class FilterParams(
    val difficulty: Int,
    val category: String,
    val status: StatusFilter,
    val searchQuery: String
)

data class SunnahListUiState(
    val sunnahs: List<SunnahWithHadith> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedDifficulty: Int = 0, // 0 = all
    val selectedCategory: String = "الكل",
    val selectedStatus: StatusFilter = StatusFilter.ALL,
    val searchQuery: String = "",
    val completedSunnahIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true
)

class SunnahListViewModel(
    private val sunnahRepository: SunnahRepository
) : ViewModel() {

    private val _allSunnahs = sunnahRepository.getAllSunnahsWithHadith()
    private val _allCategories = sunnahRepository.getAllCategories()
    private val _userProgress = sunnahRepository.getUserProgress()

    private val _selectedDifficulty = MutableStateFlow(0)
    private val _selectedCategory = MutableStateFlow("الكل")
    private val _selectedStatus = MutableStateFlow(StatusFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    private val _filterParams = combine(
        _selectedDifficulty,
        _selectedCategory,
        _selectedStatus,
        _searchQuery
    ) { difficulty, category, status, query ->
        FilterParams(difficulty, category, status, query)
    }

    val uiState: StateFlow<SunnahListUiState> = combine(
        _allSunnahs,
        _allCategories,
        _userProgress,
        _filterParams
    ) { sunnahs, categories, progress, filters ->
        val completedSet = SunnahRepository.parseCompletedSunnahIds(progress?.completedSunnahs ?: "[]")

        val filtered = sunnahs.filter { item ->
            val matchDifficulty = filters.difficulty == 0 || item.sunnah.difficulty == filters.difficulty
            val matchCategory = filters.category == "الكل" || item.sunnah.category == filters.category
            val matchStatus = when (filters.status) {
                StatusFilter.ALL -> true
                StatusFilter.COMPLETED -> completedSet.contains(item.sunnah.id)
                StatusFilter.UNCOMPLETED -> !completedSet.contains(item.sunnah.id)
            }
            val matchQuery = filters.searchQuery.isBlank() ||
                item.sunnah.title.contains(filters.searchQuery, ignoreCase = true) ||
                item.sunnah.description.contains(filters.searchQuery, ignoreCase = true) ||
                item.sunnah.orderIndex.toString() == filters.searchQuery.trim()

            matchDifficulty && matchCategory && matchStatus && matchQuery
        }

        SunnahListUiState(
            sunnahs = filtered,
            categories = listOf("الكل") + categories,
            selectedDifficulty = filters.difficulty,
            selectedCategory = filters.category,
            selectedStatus = filters.status,
            searchQuery = filters.searchQuery,
            completedSunnahIds = completedSet,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SunnahListUiState()
    )

    fun setDifficulty(difficulty: Int) {
        _selectedDifficulty.value = difficulty
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setStatus(status: StatusFilter) {
        _selectedStatus.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
                    return SunnahListViewModel(sunnahRepository) as T
                }
            }
    }
}
