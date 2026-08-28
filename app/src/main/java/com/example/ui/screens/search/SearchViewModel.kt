package com.example.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Hadith
import com.example.data.repository.SunnahRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val selectedCollection: String = "ALL", // "ALL", "SAHIH_BUKHARI", "SAHIH_MUSLIM"
    val results: List<Hadith> = emptyList(),
    val totalHadithCount: Int = 14736,
    val isSearching: Boolean = false
)

class SearchViewModel(
    private val sunnahRepository: SunnahRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedCollection = MutableStateFlow("ALL")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        _selectedCollection,
        sunnahRepository.getHadithsCount()
    ) { query, collection, totalCount ->
        Triple(query, collection, totalCount)
    }.flatMapLatest { (query, collection, totalCount) ->
        if (query.trim().length < 2) {
            flowOf(
                SearchUiState(
                    query = query,
                    selectedCollection = collection,
                    results = emptyList(),
                    totalHadithCount = totalCount,
                    isSearching = false
                )
            )
        } else {
            sunnahRepository.searchHadiths(query, collection).debounce(150).distinctUntilChanged().flatMapLatest { hadiths ->
                flowOf(
                    SearchUiState(
                        query = query,
                        selectedCollection = collection,
                        results = hadiths,
                        totalHadithCount = totalCount,
                        isSearching = false
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun setQuery(query: String) {
        _query.value = query
    }

    fun setCollection(collection: String) {
        _selectedCollection.value = collection
    }

    companion object {
        fun provideFactory(sunnahRepository: SunnahRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(sunnahRepository) as T
                }
            }
    }
}
