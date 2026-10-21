package com.englishlearningapp.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.question.AdminQuestionDto
import com.englishlearningapp.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val expandedQuestionId: Int? = null,
    val previewQuestion: AdminQuestionDto? = null,
    val isQuestionLoading: Boolean = false,
    val questionError: String? = null
)

class ResultViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadQuestionDetail(questionId: Int) {
        val current = _uiState.value
        if (current.expandedQuestionId == questionId) {
            // Collapse
            _uiState.value = current.copy(
                expandedQuestionId = null,
                previewQuestion = null,
                isQuestionLoading = false,
                questionError = null
            )
        } else {
            // Expand and Load
            _uiState.value = current.copy(
                expandedQuestionId = questionId,
                isQuestionLoading = true,
                previewQuestion = null,
                questionError = null
            )
            viewModelScope.launch {
                try {
                    val question = questionRepository.getQuestion(questionId)
                    if (_uiState.value.expandedQuestionId == questionId) {
                        _uiState.value = _uiState.value.copy(
                            isQuestionLoading = false,
                            previewQuestion = question
                        )
                    }
                } catch (e: Exception) {
                    if (_uiState.value.expandedQuestionId == questionId) {
                        _uiState.value = _uiState.value.copy(
                            isQuestionLoading = false,
                            questionError = e.toBackendMessage()
                        )
                    }
                }
            }
        }
    }

    fun dismissQuestionDetail() {
        _uiState.value = _uiState.value.copy(
            expandedQuestionId = null,
            previewQuestion = null,
            isQuestionLoading = false,
            questionError = null
        )
    }

    class Factory(
        private val questionRepository: QuestionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
                return ResultViewModel(questionRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
