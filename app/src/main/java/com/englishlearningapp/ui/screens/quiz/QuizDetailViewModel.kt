package com.englishlearningapp.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestDetailDto
import com.englishlearningapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TestAvailability {
    UPCOMING,
    OPEN,
    EXPIRED,
    AVAILABLE
}

data class QuizDetailUiState(
    val test: CollectionTestDetailDto? = null,
    val availability: TestAvailability = TestAvailability.AVAILABLE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeAttemptId: Int? = null
)

class QuizDetailViewModel(
    private val quizRepository: QuizRepository,
    private val testId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizDetailUiState())
    val uiState: StateFlow<QuizDetailUiState> = _uiState.asStateFlow()

    init {
        loadTestDetail()
    }

    fun loadTestDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val test = quizRepository.getTestDetail(testId)
                
                // Fetch attempts to check for any unfinished attempt
                val attemptsResult = quizRepository.getMyAttempts(1)
                val activeAttempt = attemptsResult.first.find {
                    it.collectionTest?.id == testId && it.status != "submitted"
                }

                _uiState.value = QuizDetailUiState(
                    test = test,
                    availability = resolveAvailability(test.startedAt, test.finishedAt),
                    activeAttemptId = activeAttempt?.id
                )
            } catch (exception: Exception) {
                _uiState.value = QuizDetailUiState(
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    private fun resolveAvailability(startedAt: String?, finishedAt: String?): TestAvailability {
        val now = LocalDateTime.now()
        val startTime = parseDateTime(startedAt)
        val finishTime = parseDateTime(finishedAt)

        return when {
            finishTime != null && now.isAfter(finishTime) -> TestAvailability.EXPIRED
            startTime != null && now.isBefore(startTime) -> TestAvailability.UPCOMING
            startTime != null || finishTime != null -> TestAvailability.OPEN
            else -> TestAvailability.AVAILABLE
        }
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val normalized = value.replace('T', ' ').substringBefore('.').removeSuffix("Z")
            LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }.getOrNull()
    }

    class Factory(
        private val quizRepository: QuizRepository,
        private val testId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuizDetailViewModel::class.java)) {
                return QuizDetailViewModel(quizRepository, testId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
