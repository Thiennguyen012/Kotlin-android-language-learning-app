package com.englishlearningapp.ui.screens.admin.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestDto
import com.englishlearningapp.data.repository.QuizRepository
import com.englishlearningapp.core.network.toBackendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TestManagementUiState(
    val tests: List<CollectionTestDto> = emptyList(),
    val searchQuery: String = "",
    val appliedSearch: String = "",
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val deletingTest: CollectionTestDto? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class TestManagementViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TestManagementUiState())
    val uiState: StateFlow<TestManagementUiState> = _uiState.asStateFlow()

    init { loadPage(1) }

    fun updateSearch(value: String) { _uiState.value = _uiState.value.copy(searchQuery = value) }

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
        if (!state.isLoading && state.currentPage < state.lastPage) loadPage(state.currentPage + 1)
    }
    fun previousPage() {
        val state = _uiState.value
        if (!state.isLoading && state.currentPage > 1) loadPage(state.currentPage - 1)
    }

    fun requestDelete(test: CollectionTestDto) {
        _uiState.value = _uiState.value.copy(deletingTest = test)
    }
    fun dismissDelete() {
        if (!_uiState.value.isDeleting) _uiState.value = _uiState.value.copy(deletingTest = null)
    }
    fun confirmDelete() {
        val test = _uiState.value.deletingTest ?: return
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                val result = repository.deleteTest(test.id)
                val targetPage = if (_uiState.value.tests.size == 1 && _uiState.value.currentPage > 1) {
                    _uiState.value.currentPage - 1
                } else _uiState.value.currentPage
                _uiState.value = _uiState.value.copy(
                    deletingTest = null,
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
    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val (tests, meta) = repository.getTests(page, search = _uiState.value.appliedSearch)
                _uiState.value = _uiState.value.copy(
                    tests = tests,
                    currentPage = meta?.currentPage ?: page,
                    lastPage = meta?.lastPage ?: 1,
                    totalItems = meta?.total ?: tests.size,
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

    class Factory(private val repository: QuizRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TestManagementViewModel::class.java)) {
                return TestManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
