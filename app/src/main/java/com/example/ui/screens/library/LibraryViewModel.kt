package com.example.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.PdfBook
import com.example.data.repository.SunnahRepository
import com.example.ui.utils.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val books: List<PdfBook> = emptyList(),
    val isAdminAuthenticated: Boolean = false,
    val isLoading: Boolean = true
)

class LibraryViewModel(
    private val sunnahRepository: SunnahRepository
) : ViewModel() {

    private val _books = sunnahRepository.getAllPdfBooks()
    private val _isAdminAuthenticated = MutableStateFlow(false)

    val uiState: StateFlow<LibraryUiState> = combine(
        _books,
        _isAdminAuthenticated
    ) { books, isAdmin ->
        LibraryUiState(
            books = books,
            isAdminAuthenticated = isAdmin,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun authenticateAdmin(password: String): Boolean {
        val isValid = PasswordHasher.verifyPassword(password)
        if (isValid) {
            _isAdminAuthenticated.value = true
        }
        return isValid
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun deleteBook(id: Int) {
        viewModelScope.launch {
            sunnahRepository.deletePdfBook(id)
        }
    }

    fun addBook(title: String, description: String, filename: String, size: Long) {
        viewModelScope.launch {
            val book = PdfBook(
                title = title,
                description = description,
                filename = filename,
                size = size,
                addedDate = System.currentTimeMillis(),
                isBuiltin = false
            )
            sunnahRepository.insertPdfBook(book)
        }
    }

    companion object {
        fun provideFactory(sunnahRepository: SunnahRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(sunnahRepository) as T
                }
            }
    }
}
