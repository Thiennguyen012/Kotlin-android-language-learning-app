package com.englishlearningapp.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.quiz.AttemptHistoryDto
import com.englishlearningapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AttemptHistoryUiState(
    val attempts: List<AttemptHistoryDto> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val totalAttempts: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AttemptHistoryViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttemptHistoryUiState())
    val uiState: StateFlow<AttemptHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPage(1)
    }

    fun loadHistory() {
        loadPage(_uiState.value.currentPage)
    }

    fun nextPage() {
        val current = _uiState.value
        if (current.currentPage < current.lastPage && !current.isLoading) {
            loadPage(current.currentPage + 1)
        }
    }

    fun previousPage() {
        val current = _uiState.value
        if (current.currentPage > 1 && !current.isLoading) {
            loadPage(current.currentPage - 1)
        }
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            try {
                val (attempts, meta) = quizRepository.getMyAttempts(page)
                _uiState.value = _uiState.value.copy(
                    attempts = attempts,
                    currentPage = meta?.currentPage ?: page,
                    lastPage = meta?.lastPage ?: 1,
                    totalAttempts = meta?.total ?: attempts.size,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    class Factory(private val quizRepository: QuizRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AttemptHistoryViewModel::class.java)) {
                return AttemptHistoryViewModel(quizRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
