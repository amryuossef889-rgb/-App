package com.example.ui.screens.library

import android.content.Context
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
    private val sunnahRepository: SunnahRepository,
    private val appContext: Context
) : ViewModel() {
    private val _books = sunnahRepository.getAllPdfBooks()
    private val _isAdminAuthenticated = MutableStateFlow(false)

    val uiState: StateFlow<LibraryUiState> = combine(_books, _isAdminAuthenticated) { books, isAdmin ->
        LibraryUiState(books = books, isAdminAuthenticated = isAdmin, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun isAdminPasswordConfigured(): Boolean = PasswordHasher.isConfigured(appContext)

    /** First use creates a device-local passphrase; later uses verify it. */
    fun authenticateAdmin(password: String): Boolean {
        val isValid = PasswordHasher.verifyOrCreate(appContext, password)
        if (isValid) _isAdminAuthenticated.value = true
        return isValid
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun deleteBook(id: Int) {
        viewModelScope.launch { sunnahRepository.deletePdfBook(id) }
    }

    fun addBook(title: String, description: String, filename: String, size: Long) {
        if (title.isBlank() || filename.isBlank() || size <= 0L) return
        viewModelScope.launch {
            sunnahRepository.insertPdfBook(
                PdfBook(
                    title = title.trim().take(160),
                    description = description.trim().take(1000),
                    filename = filename,
                    size = size,
                    addedDate = System.currentTimeMillis(),
                    isBuiltin = false
                )
            )
        }
    }

    companion object {
        fun provideFactory(
            sunnahRepository: SunnahRepository,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LibraryViewModel(sunnahRepository, context.applicationContext) as T
            }
        }
    }
}
