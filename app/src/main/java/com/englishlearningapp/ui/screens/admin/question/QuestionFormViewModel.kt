package com.englishlearningapp.ui.screens.admin.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.englishlearningapp.data.remote.dto.question.AdminQuestionDto
import com.englishlearningapp.data.remote.dto.question.QuestionRequestDto
import com.englishlearningapp.data.remote.dto.question.QuestionTypeDto
import com.englishlearningapp.data.repository.QuestionRepository
import com.englishlearningapp.core.network.toBackendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MatchingPairInput(val left: String = "", val right: String = "")

data class QuestionFormUiState(
    val questionText: String = "",
    val questionTypes: List<QuestionTypeDto> = emptyList(),
    val selectedTypeId: Int? = null,
    val options: List<String> = listOf("", "", "", ""),
    val correctOptionIndex: Int = 0,
    val trueFalseAnswer: Boolean = true,
    val fillBlankAnswer: String = "",
    val matchingPairs: List<MatchingPairInput> = listOf(MatchingPairInput(), MatchingPairInput()),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
) {
    val selectedType: QuestionTypeDto?
        get() = questionTypes.find { it.id == selectedTypeId }
}

class QuestionFormViewModel(
    private val repository: QuestionRepository,
    private val questionId: Int?
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuestionFormUiState())
    val uiState: StateFlow<QuestionFormUiState> = _uiState.asStateFlow()

    init { loadInitialData() }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val types = repository.getQuestionTypes()
                _uiState.value = _uiState.value.copy(questionTypes = types)
                questionId?.let { populateQuestion(repository.getQuestion(it)) }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun updateQuestionText(value: String) {
        _uiState.value = _uiState.value.copy(questionText = value)
        clearValidation("questionText")
    }

    fun selectType(typeId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTypeId = typeId,
            options = listOf("", "", "", ""),
            correctOptionIndex = 0,
            trueFalseAnswer = true,
            fillBlankAnswer = "",
            matchingPairs = listOf(MatchingPairInput(), MatchingPairInput())
        )
        clearValidation("questionType")
        clearValidation("questionData")
    }

    fun updateOption(index: Int, value: String) {
        val options = _uiState.value.options.toMutableList()
        if (index in options.indices) options[index] = value
        _uiState.value = _uiState.value.copy(options = options)
        clearValidation("questionData")
    }

    fun addOption() {
        _uiState.value = _uiState.value.copy(options = _uiState.value.options + "")
    }

    fun removeOption(index: Int) {
        val state = _uiState.value
        if (state.options.size <= 2 || index !in state.options.indices) return
        val options = state.options.toMutableList().apply { removeAt(index) }
        _uiState.value = state.copy(
            options = options,
            correctOptionIndex = state.correctOptionIndex.coerceAtMost(options.lastIndex)
        )
    }

    fun selectCorrectOption(index: Int) {
        _uiState.value = _uiState.value.copy(correctOptionIndex = index)
    }

    fun updateTrueFalseAnswer(value: Boolean) {
        _uiState.value = _uiState.value.copy(trueFalseAnswer = value)
    }

    fun updateFillBlankAnswer(value: String) {
        _uiState.value = _uiState.value.copy(fillBlankAnswer = value)
        clearValidation("questionData")
    }

    fun updateMatchingLeft(index: Int, value: String) = updateMatchingPair(index, left = value)

    fun updateMatchingRight(index: Int, value: String) = updateMatchingPair(index, right = value)

    private fun updateMatchingPair(index: Int, left: String? = null, right: String? = null) {
        val pairs = _uiState.value.matchingPairs.toMutableList()
        if (index !in pairs.indices) return
        val pair = pairs[index]
        pairs[index] = pair.copy(left = left ?: pair.left, right = right ?: pair.right)
        _uiState.value = _uiState.value.copy(matchingPairs = pairs)
        clearValidation("questionData")
    }

    fun addMatchingPair() {
        _uiState.value = _uiState.value.copy(
            matchingPairs = _uiState.value.matchingPairs + MatchingPairInput()
        )
    }

    fun removeMatchingPair(index: Int) {
        val state = _uiState.value
        if (state.matchingPairs.size <= 1 || index !in state.matchingPairs.indices) return
        _uiState.value = state.copy(
            matchingPairs = state.matchingPairs.toMutableList().apply { removeAt(index) }
        )
    }

    fun saveQuestion() {
        val state = _uiState.value
        if (state.isSaving) return
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(validationErrors = errors)
            return
        }

        val request = QuestionRequestDto(
            questionTypeId = state.selectedTypeId!!,
            questionText = state.questionText.trim(),
            questionData = buildQuestionData(state)
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val result = if (questionId == null) repository.createQuestion(request)
                else repository.updateQuestion(questionId, request)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validate(state: QuestionFormUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.questionText.isBlank()) errors["questionText"] = "Question text is required"
        if (state.questionText.length > 255) errors["questionText"] = "Maximum 255 characters"
        val keyword = state.selectedType?.keyword
        if (keyword == null) errors["questionType"] = "Question type is required"
        when (keyword) {
            "multiple_choice" -> if (state.options.size < 2 || state.options.any { it.isBlank() }) {
                errors["questionData"] = "Enter at least two non-empty options"
            } else if (state.options.map { it.trim().lowercase() }.distinct().size != state.options.size) {
                errors["questionData"] = "Answer options must not be duplicated"
            }
            "fill_in_blank" -> if (state.fillBlankAnswer.isBlank()) {
                errors["questionData"] = "Answer is required"
            }
            "matching" -> if (state.matchingPairs.isEmpty() || state.matchingPairs.any {
                    it.left.isBlank() || it.right.isBlank()
                }) {
                errors["questionData"] = "Every matching pair must be complete"
            }
        }
        return errors
    }

    private fun buildQuestionData(state: QuestionFormUiState): JsonObject = JsonObject().apply {
        when (state.selectedType?.keyword) {
            "multiple_choice" -> {
                add("options", JsonArray().apply { state.options.forEach { add(it.trim()) } })
                addProperty("correct", state.options[state.correctOptionIndex].trim())
            }
            "true_false" -> addProperty("correct", state.trueFalseAnswer)
            "fill_in_blank" -> addProperty("answer", state.fillBlankAnswer.trim())
            "matching" -> add("pairs", JsonArray().apply {
                state.matchingPairs.forEach { pair ->
                    add(JsonObject().apply {
                        addProperty("left", pair.left.trim())
                        addProperty("right", pair.right.trim())
                    })
                }
            })
        }
    }

    private fun populateQuestion(question: AdminQuestionDto) {
        val keyword = question.questionType?.keyword
        val questionData = question.getQuestionDataObject()
        var state = _uiState.value.copy(
            questionText = question.questionText,
            selectedTypeId = question.questionType?.id
        )
        when (keyword) {
            "multiple_choice" -> {
                val options = questionData.getAsJsonArray("options")?.map { it.asString }.orEmpty()
                val correct = questionData.get("correct")
                val correctIndex = when {
                    correct == null -> 0
                    correct.asJsonPrimitive.isNumber -> correct.asInt
                    else -> options.indexOf(correct.asString).coerceAtLeast(0)
                }
                state = state.copy(options = options, correctOptionIndex = correctIndex)
            }
            "true_false" -> state = state.copy(
                trueFalseAnswer = questionData.get("correct")?.asBoolean ?: true
            )
            "fill_in_blank" -> state = state.copy(
                fillBlankAnswer = questionData.get("answer")?.asString.orEmpty()
            )
            "matching" -> state = state.copy(
                matchingPairs = questionData.getAsJsonArray("pairs")?.map { element ->
                    val pair = element.asJsonObject
                    MatchingPairInput(
                        left = pair.get("left")?.asString.orEmpty(),
                        right = pair.get("right")?.asString.orEmpty()
                    )
                }.orEmpty().ifEmpty { listOf(MatchingPairInput()) }
            )
        }
        _uiState.value = state
    }

    private fun clearValidation(key: String) {
        _uiState.value = _uiState.value.copy(
            validationErrors = _uiState.value.validationErrors - key
        )
    }

    class Factory(
        private val repository: QuestionRepository,
        private val questionId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuestionFormViewModel::class.java)) {
                return QuestionFormViewModel(repository, questionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
