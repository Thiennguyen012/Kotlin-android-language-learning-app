package com.englishlearningapp.ui.screens.admin.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.data.remote.dto.question.AdminQuestionDto
import com.englishlearningapp.data.repository.QuestionRepository
import com.englishlearningapp.core.network.toBackendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionManagementUiState(
    val questions: List<AdminQuestionDto> = emptyList(),
    val searchQuery: String = "",
    val appliedSearch: String = "",
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val deletingQuestion: AdminQuestionDto? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class QuestionManagementViewModel(
    private val repository: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuestionManagementUiState())
    val uiState: StateFlow<QuestionManagementUiState> = _uiState.asStateFlow()

    init { loadPage(1) }

    fun updateSearchQuery(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
    }

    fun applySearch() {
        _uiState.value = _uiState.value.copy(appliedSearch = _uiState.value.searchQuery.trim())
        loadPage(1)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", appliedSearch = "")
        loadPage(1)
    }

    fun refresh() = loadPage(_uiState.value.currentPage)

    fun nextPage() {
        val state = _uiState.value
        if (state.currentPage < state.lastPage && !state.isLoading) loadPage(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value
        if (state.currentPage > 1 && !state.isLoading) loadPage(state.currentPage - 1)
    }

    fun requestDelete(question: AdminQuestionDto) {
        _uiState.value = _uiState.value.copy(deletingQuestion = question)
    }

    fun dismissDelete() {
        if (!_uiState.value.isDeleting) _uiState.value = _uiState.value.copy(deletingQuestion = null)
    }

    fun confirmDelete() {
        val question = _uiState.value.deletingQuestion ?: return
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                val result = repository.deleteQuestion(question.id)
                val targetPage = if (_uiState.value.questions.size == 1 && _uiState.value.currentPage > 1) {
                    _uiState.value.currentPage - 1
                } else _uiState.value.currentPage
                _uiState.value = _uiState.value.copy(
                    deletingQuestion = null,
                    isDeleting = false,
                    successMessage = result.message
                )
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
                val (questions, meta) = repository.getQuestions(page, search = _uiState.value.appliedSearch)
                _uiState.value = _uiState.value.copy(
                    questions = questions,
                    currentPage = meta?.currentPage ?: page,
                    lastPage = meta?.lastPage ?: 1,
                    totalItems = meta?.total ?: questions.size,
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

    class Factory(private val repository: QuestionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuestionManagementViewModel::class.java)) {
                return QuestionManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
