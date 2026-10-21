package com.englishlearningapp.ui.screens.admin.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardDto
import com.englishlearningapp.data.repository.FlashcardRepository
import com.englishlearningapp.core.network.toBackendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FlashcardManagementUiState(
    val flashcards: List<FlashcardDto> = emptyList(),
    val searchQuery: String = "",
    val appliedSearch: String = "",
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val deletingFlashcard: FlashcardDto? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class FlashcardManagementViewModel(
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardManagementUiState())
    val uiState: StateFlow<FlashcardManagementUiState> = _uiState.asStateFlow()

    init {
        loadPage(1)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun applySearch() {
        _uiState.value = _uiState.value.copy(appliedSearch = _uiState.value.searchQuery.trim())
        loadPage(1)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", appliedSearch = "")
        loadPage(1)
    }

    fun refresh() {
        loadPage(_uiState.value.currentPage)
    }

    fun nextPage() {
        val state = _uiState.value
        if (!state.isLoading && state.currentPage < state.lastPage) loadPage(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value
        if (!state.isLoading && state.currentPage > 1) loadPage(state.currentPage - 1)
    }

    fun requestDelete(flashcard: FlashcardDto) {
        _uiState.value = _uiState.value.copy(deletingFlashcard = flashcard)
    }

    fun dismissDelete() {
        if (!_uiState.value.isDeleting) {
            _uiState.value = _uiState.value.copy(deletingFlashcard = null)
        }
    }

    fun confirmDelete() {
        val flashcard = _uiState.value.deletingFlashcard ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                val result = flashcardRepository.deleteFlashcard(flashcard.id)
                _uiState.value = _uiState.value.copy(
                    deletingFlashcard = null,
                    isDeleting = false,
                    successMessage = result.message
                )
                val targetPage = if (_uiState.value.flashcards.size == 1 && _uiState.value.currentPage > 1) {
                    _uiState.value.currentPage - 1
                } else {
                    _uiState.value.currentPage
                }
                loadPage(targetPage)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val (flashcards, meta) = flashcardRepository.getFlashcards(
                    page = page,
                    search = _uiState.value.appliedSearch
                )
                _uiState.value = _uiState.value.copy(
                    flashcards = flashcards,
                    currentPage = meta?.currentPage ?: page,
                    lastPage = meta?.lastPage ?: 1,
                    totalItems = meta?.total ?: flashcards.size,
                    isLoading = false
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    class Factory(private val repository: FlashcardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FlashcardManagementViewModel::class.java)) {
                return FlashcardManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
