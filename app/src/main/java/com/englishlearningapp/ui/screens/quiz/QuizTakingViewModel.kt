package com.englishlearningapp.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.quiz.AttemptDetailDto
import com.englishlearningapp.data.remote.dto.quiz.QuestionDto
import com.englishlearningapp.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizTakingUiState(
    val attemptId: Int? = null,
    val testName: String = "",
    val questions: List<QuestionDto> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, String> = emptyMap(),
    val remainingSeconds: Int = 0,
    val isLoading: Boolean = false,
    val isSavingAnswer: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val answerErrorMessage: String? = null,
    val isFinished: Boolean = false,
    val isAlreadySubmitted: Boolean = false
) {
    val currentQuestion: QuestionDto?
        get() = questions.getOrNull(currentIndex)

    val currentSelectedAnswer: String?
        get() = currentQuestion?.let { selectedAnswers[it.id] }

    val hasCurrentAnswer: Boolean
        get() = !currentSelectedAnswer.isNullOrBlank()

    val progress: String
        get() = if (questions.isEmpty()) "0 / 0" else "${currentIndex + 1} / ${questions.size}"

    val isLastQuestion: Boolean
        get() = currentIndex == questions.size - 1

    val isFirstQuestion: Boolean
        get() = currentIndex == 0

    val formattedRemainingTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

class QuizTakingViewModel(
    private val quizRepository: QuizRepository,
    private val testId: Int,
    private val canAnswer: Boolean,
    private val canSubmit: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizTakingUiState())
    val uiState: StateFlow<QuizTakingUiState> = _uiState.asStateFlow()
    private var timerJob: Job? = null

    init {
        startAttempt()
    }

    private fun startAttempt() {
        viewModelScope.launch {
            _uiState.value = QuizTakingUiState(isLoading = true)
            try {
                val startedAttempt = quizRepository.startAttempt(testId)
                val attemptDetail = quizRepository.getAttempt(startedAttempt.attemptId)
                if (attemptDetail.attempt.status == "submitted") {
                    _uiState.value = QuizTakingUiState(isAlreadySubmitted = true)
                    return@launch
                }
                val questions = quizRepository.getAttemptQuestions(startedAttempt.attemptId)
                val savedAnswers = questions.mapNotNull { question ->
                    question.userAnswer?.let { question.id to it }
                }.toMap()
                val firstUnansweredIndex = questions.indexOfFirst { it.userAnswer == null }

                _uiState.value = QuizTakingUiState(
                    attemptId = startedAttempt.attemptId,
                    testName = attemptDetail.attempt.collectionTest?.testName.orEmpty(),
                    questions = questions,
                    currentIndex = firstUnansweredIndex.takeIf { it >= 0 } ?: 0,
                    selectedAnswers = savedAnswers,
                    remainingSeconds = attemptDetail.remainingSeconds
                )
                startTimer()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun updateAnswer(answer: String) {
        if (!canAnswer) return
        val current = _uiState.value
        val question = current.currentQuestion ?: return
        _uiState.value = current.copy(
            selectedAnswers = current.selectedAnswers + (question.id to answer),
            answerErrorMessage = null
        )
    }

    fun saveAnswerAndContinue() {
        if (!canAnswer) return
        val current = _uiState.value
        val attemptId = current.attemptId ?: return
        val question = current.currentQuestion ?: return
        val answer = current.currentSelectedAnswer ?: return
        if (current.isSavingAnswer || current.isSubmitting) return

        _uiState.value = current.copy(isSavingAnswer = true, answerErrorMessage = null)

        viewModelScope.launch {
            try {
                quizRepository.saveAnswer(
                    attemptId = attemptId,
                    questionId = question.id,
                    userAnswer = question.toApiAnswer(answer)
                )
                val latest = _uiState.value
                _uiState.value = latest.copy(isSavingAnswer = false)
                if (latest.isLastQuestion) {
                    submitAttempt()
                } else {
                    nextQuestion()
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavingAnswer = false,
                    answerErrorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun nextQuestion() {
        val current = _uiState.value
        if (!current.isSavingAnswer && current.currentIndex < current.questions.size - 1) {
            _uiState.value = current.copy(
                currentIndex = current.currentIndex + 1,
                answerErrorMessage = null
            )
        }
    }

    fun previousQuestion() {
        val current = _uiState.value
        if (!current.isSavingAnswer && current.currentIndex > 0) {
            _uiState.value = current.copy(
                currentIndex = current.currentIndex - 1,
                answerErrorMessage = null
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isFinished) {
                delay(1_000)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = (_uiState.value.remainingSeconds - 1).coerceAtLeast(0)
                )
            }
            if (!_uiState.value.isFinished) {
                submitAttempt()
            }
        }
    }

    private fun submitAttempt() {
        if (!canSubmit) return
        val current = _uiState.value
        val attemptId = current.attemptId ?: return
        if (current.isSubmitting || current.isFinished) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            try {
                var submitError: Exception? = null
                try {
                    quizRepository.submitAttempt(attemptId)
                } catch (exception: Exception) {
                    submitError = exception
                }

                val detail = quizRepository.getAttempt(attemptId)
                if (detail.attempt.status != "submitted") {
                    throw submitError ?: Exception("Quiz could not be submitted")
                }

                storeResult(detail)
                timerJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    remainingSeconds = 0,
                    isFinished = true
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun checkAttemptStatus() {
        val attemptId = _uiState.value.attemptId ?: return
        viewModelScope.launch {
            try {
                val attemptDetail = quizRepository.getAttempt(attemptId)
                if (attemptDetail.attempt.status == "submitted") {
                    _uiState.value = _uiState.value.copy(isAlreadySubmitted = true)
                }
            } catch (e: Exception) {
                // Ignore background check errors
            }
        }
    }

    private fun storeResult(detail: AttemptDetailDto) {
        val attempt = detail.attempt
        val reviewItems = attempt.questions.map { question ->
            val userAnswer = question.formatUserAnswer(question.answer?.userAnswer)
            val correctAnswer = question.getCorrectAnswer()
            ReviewItem(
                questionId = question.id,
                questionText = question.questionText,
                userAnswer = userAnswer,
                correctAnswer = correctAnswer,
                isCorrect = question.answer?.isCorrect == true
            )
        }
        QuizResultHolder.result = QuizResult(
            testName = attempt.collectionTest?.testName ?: _uiState.value.testName,
            score = attempt.correctCount,
            total = attempt.collectionTest?.totalQuestions ?: reviewItems.size,
            reviewItems = reviewItems,
            startedTime = attempt.startedTime,
            finishedTime = attempt.finishedTime,
            totalTime = attempt.totalTime
        )
    }

    class Factory(
        private val quizRepository: QuizRepository,
        private val testId: Int,
        private val canAnswer: Boolean,
        private val canSubmit: Boolean
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuizTakingViewModel::class.java)) {
                return QuizTakingViewModel(
                    quizRepository,
                    testId,
                    canAnswer,
                    canSubmit
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
