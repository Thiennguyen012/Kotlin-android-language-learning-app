package com.englishlearningapp.ui.screens.admin.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.data.repository.FlashcardRepository
import com.englishlearningapp.core.network.toBackendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashcardFormViewModel(
    private val flashcardRepository: FlashcardRepository,
    private val collectionId: Int?,
    private val flashcardId: Int? // null means Create mode
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardFormUiState())
    val uiState: StateFlow<FlashcardFormUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingWordTypes = true, errorMessage = null)
            try {
                // Load word types
                val types = flashcardRepository.getWordTypes()
                _uiState.value = _uiState.value.copy(
                    wordTypes = types,
                    isLoadingWordTypes = false
                )

                // If edit mode, load flashcard details
                if (flashcardId != null) {
                    _uiState.value = _uiState.value.copy(isLoadingFlashcard = true)
                    val flashcard = flashcardRepository.getFlashcard(flashcardId)
                    _uiState.value = _uiState.value.copy(
                        originalWord = flashcard.originalWord,
                        translatedWord = flashcard.translatedWord,
                        explanation = flashcard.explanation.orEmpty(),
                        selectedWordTypeId = flashcard.wordType?.id,
                        isLoadingFlashcard = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingWordTypes = false,
                    isLoadingFlashcard = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    fun updateOriginalWord(word: String) {
        _uiState.value = _uiState.value.copy(originalWord = word)
        clearError("originalWord")
    }

    fun updateTranslatedWord(word: String) {
        _uiState.value = _uiState.value.copy(translatedWord = word)
        clearError("translatedWord")
    }

    fun updateExplanation(value: String) {
        _uiState.value = _uiState.value.copy(explanation = value, errorMessage = null)
    }

    fun updateWordType(typeId: Int) {
        _uiState.value = _uiState.value.copy(selectedWordTypeId = typeId)
        clearError("wordType")
    }

    private fun clearError(field: String) {
        val currentErrors = _uiState.value.validationErrors.toMutableMap()
        currentErrors.remove(field)
        _uiState.value = _uiState.value.copy(validationErrors = currentErrors)
    }

    fun saveFlashcard() {
        val state = _uiState.value
        if (state.isSaving) return

        val original = state.originalWord.trim()
        val translated = state.translatedWord.trim()
        val explanation = state.explanation.trim().takeIf { it.isNotEmpty() }
        val wordTypeId = state.selectedWordTypeId

        // Validation
        val errors = mutableMapOf<String, String>()
        if (original.isEmpty()) errors["originalWord"] = "Original Word is required"
        if (translated.isEmpty()) errors["translatedWord"] = "Translated Word is required"
        if (wordTypeId == null) errors["wordType"] = "Word Type is required"

        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(validationErrors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                if (flashcardId == null) {
                    // Create mode
                    val result = flashcardRepository.createFlashcard(
                        original,
                        translated,
                        wordTypeId!!,
                        explanation
                    )
                    collectionId?.let { id ->
                        flashcardRepository.attachToCollection(id, listOf(result.data.id))
                    }
                    _uiState.value = _uiState.value.copy(successMessage = result.message)
                } else {
                    // Edit mode
                    val result = flashcardRepository.updateFlashcard(
                        flashcardId,
                        original,
                        translated,
                        wordTypeId!!,
                        explanation
                    )
                    _uiState.value = _uiState.value.copy(successMessage = result.message)
                }
                
                _uiState.value = _uiState.value.copy(isSaving = false, isSaveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val flashcardRepository: FlashcardRepository,
        private val collectionId: Int?,
        private val flashcardId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FlashcardFormViewModel::class.java)) {
                return FlashcardFormViewModel(flashcardRepository, collectionId, flashcardId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
