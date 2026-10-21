package com.englishlearningapp.ui.screens.admin.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.data.remote.dto.question.AdminQuestionDto
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestRequestDto
import com.englishlearningapp.data.remote.dto.quiz.TestTypeDto
import com.englishlearningapp.data.repository.FlashcardRepository
import com.englishlearningapp.data.repository.QuestionRepository
import com.englishlearningapp.data.repository.QuizRepository
import com.englishlearningapp.core.network.toBackendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TestFormUiState(
    val testTypes: List<TestTypeDto> = emptyList(),
    val collections: List<FlashcardCollectionDto> = emptyList(),
    val questions: List<AdminQuestionDto> = emptyList(),
    val selectedTestTypeId: Int? = null,
    val selectedCollectionId: Int? = null,
    val testName: String = "",
    val description: String = "",
    val duration: String = "",
    val status: Int = 1,
    val startedAt: String = "",
    val finishedAt: String = "",
    val selectedQuestionIds: Set<Int> = emptySet(),
    val questionSearch: String = "",
    val questionTypeFilter: String? = null,
    val showSelectedQuestionsOnly: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
) {
    val selectedTestType: TestTypeDto? get() = testTypes.find { it.id == selectedTestTypeId }
    val selectedCollection: FlashcardCollectionDto? get() = collections.find { it.id == selectedCollectionId }
    val filteredQuestions: List<AdminQuestionDto>
        get() = questions.filter {
            (questionSearch.isBlank() || it.questionText.contains(questionSearch, ignoreCase = true)) &&
                (questionTypeFilter == null || it.questionType?.keyword == questionTypeFilter) &&
                (!showSelectedQuestionsOnly || it.id in selectedQuestionIds)
        }
    val availableQuestionTypes: List<Pair<String, String>>
        get() = questions.mapNotNull { question ->
            question.questionType?.let { it.keyword to it.name }
        }.distinctBy { it.first }
}

class TestFormViewModel(
    private val quizRepository: QuizRepository,
    private val flashcardRepository: FlashcardRepository,
    private val questionRepository: QuestionRepository,
    private val testId: Int?
) : ViewModel() {
    private val _uiState = MutableStateFlow(TestFormUiState())
    val uiState: StateFlow<TestFormUiState> = _uiState.asStateFlow()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val testTypes = quizRepository.getTestTypes()
                val collections = flashcardRepository.getAllCollections()
                val questions = questionRepository.getAllQuestions()
                _uiState.value = _uiState.value.copy(
                    testTypes = testTypes,
                    collections = collections,
                    questions = questions
                )
                testId?.let { id ->
                    val test = quizRepository.getTestDetail(id)
                    _uiState.value = _uiState.value.copy(
                        selectedTestTypeId = test.testTypeId,
                        selectedCollectionId = test.collectionId,
                        testName = test.testName,
                        description = test.description.orEmpty(),
                        duration = test.duration.toString(),
                        status = test.status,
                        startedAt = test.startedAt.orEmpty(),
                        finishedAt = test.finishedAt.orEmpty(),
                        selectedQuestionIds = test.questionIds.toSet()
                    )
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun selectTestType(id: Int) = update { copy(selectedTestTypeId = id) }
    fun selectCollection(id: Int) = update { copy(selectedCollectionId = id) }
    fun updateName(value: String) = update { copy(testName = value) }
    fun updateDescription(value: String) = update { copy(description = value) }
    fun updateDuration(value: String) = update { copy(duration = value.filter(Char::isDigit)) }
    fun updateStatus(value: Int) = update { copy(status = value) }
    fun updateStartedAt(value: String) = update { copy(startedAt = value) }
    fun updateFinishedAt(value: String) = update { copy(finishedAt = value) }
    fun updateQuestionSearch(value: String) = update { copy(questionSearch = value) }
    fun updateQuestionTypeFilter(keyword: String?) = update { copy(questionTypeFilter = keyword) }
    fun updateShowSelectedOnly(value: Boolean) = update { copy(showSelectedQuestionsOnly = value) }

    fun toggleQuestion(id: Int) {
        val selected = _uiState.value.selectedQuestionIds.toMutableSet()
        if (!selected.add(id)) selected.remove(id)
        update { copy(selectedQuestionIds = selected) }
    }

    fun saveTest() {
        val state = _uiState.value
        if (state.isSaving) return
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(validationErrors = errors)
            return
        }
        val request = CollectionTestRequestDto(
            testTypeId = state.selectedTestTypeId!!,
            collectionId = state.selectedCollectionId!!,
            testName = state.testName.trim(),
            description = state.description.trim().ifBlank { null },
            duration = state.duration.toInt(),
            status = state.status,
            startedAt = state.startedAt.trim(),
            finishedAt = state.finishedAt.trim(),
            questionIds = state.selectedQuestionIds.toList()
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val result = if (testId == null) quizRepository.createTest(request)
                else quizRepository.updateTest(testId, request)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaveSuccess = true,
                    successMessage = result.message
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    private fun validate(state: TestFormUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.selectedTestTypeId == null) errors["testType"] = "Test type is required"
        if (state.selectedCollectionId == null) errors["collection"] = "Collection is required"
        if (state.testName.isBlank()) errors["name"] = "Test name is required"
        if (state.testName.length > 255) errors["name"] = "Maximum 255 characters"
        if (state.description.length > 2000) errors["description"] = "Maximum 2000 characters"
        if ((state.duration.toIntOrNull() ?: 0) < 1) errors["duration"] = "Duration must be at least 1 minute"
        val start = parseDate(state.startedAt)
        val finish = parseDate(state.finishedAt)
        if (start == null) errors["startedAt"] = "Use format yyyy-MM-dd HH:mm:ss"
        if (finish == null) errors["finishedAt"] = "Use format yyyy-MM-dd HH:mm:ss"
        if (start != null && finish != null && !finish.isAfter(start)) {
            errors["finishedAt"] = "Finish time must be after start time"
        }
        if (state.selectedQuestionIds.isEmpty()) errors["questions"] = "Select at least one question"
        return errors
    }

    private fun parseDate(value: String): LocalDateTime? = runCatching {
        LocalDateTime.parse(value.trim(), dateFormatter)
    }.getOrNull()

    private fun update(transform: TestFormUiState.() -> TestFormUiState) {
        _uiState.value = _uiState.value.transform().copy(validationErrors = emptyMap())
    }

    class Factory(
        private val quizRepository: QuizRepository,
        private val flashcardRepository: FlashcardRepository,
        private val questionRepository: QuestionRepository,
        private val testId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TestFormViewModel::class.java)) {
                return TestFormViewModel(quizRepository, flashcardRepository, questionRepository, testId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
