package com.englishlearningapp.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardDto
import com.englishlearningapp.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the flashcard study screen.
 */
data class FlashcardStudyUiState(
    val collectionName: String = "",
    val flashcards: List<FlashcardDto> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /** The flashcard currently being shown. */
    val currentFlashcard: FlashcardDto?
        get() = flashcards.getOrNull(currentIndex)

    /** Progress text like "3 / 40". */
    val progress: String
        get() = if (flashcards.isEmpty()) "0 / 0"
        else "${currentIndex + 1} / ${flashcards.size}"

    /** Whether the user can go to the previous card. */
    val canGoPrevious: Boolean
        get() = currentIndex > 0

    /** Whether the user can go to the next card. */
    val canGoNext: Boolean
        get() = currentIndex < flashcards.size - 1
}

/**
 * ViewModel for studying flashcards in a collection.
 * Handles card navigation and flip state.
 */
class FlashcardStudyViewModel(
    private val flashcardRepository: FlashcardRepository,
    private val collectionId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardStudyUiState())
    val uiState: StateFlow<FlashcardStudyUiState> = _uiState.asStateFlow()

    init {
        loadCollection()
    }

    /**
     * Load the collection detail (with flashcards) from the API.
     */
    private fun loadCollection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val detail = flashcardRepository.getCollectionDetail(collectionId)
                _uiState.value = _uiState.value.copy(
                    collectionName = detail.collectionName,
                    flashcards = detail.flashcards,
                    isLoading = false,
                    currentIndex = 0,
                    isFlipped = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    /** Flip the current card between original and translated word. */
    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    /** Go to the next flashcard. Resets flip state. */
    fun nextCard() {
        if (_uiState.value.canGoNext) {
            _uiState.value = _uiState.value.copy(
                currentIndex = _uiState.value.currentIndex + 1,
                isFlipped = false
            )
        }
    }

    /** Go to the previous flashcard. Resets flip state. */
    fun previousCard() {
        if (_uiState.value.canGoPrevious) {
            _uiState.value = _uiState.value.copy(
                currentIndex = _uiState.value.currentIndex - 1,
                isFlipped = false
            )
        }
    }

    /**
     * Factory for creating this ViewModel with dependencies.
     */
    class Factory(
        private val flashcardRepository: FlashcardRepository,
        private val collectionId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FlashcardStudyViewModel::class.java)) {
                return FlashcardStudyViewModel(flashcardRepository, collectionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
