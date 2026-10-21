package com.englishlearningapp.ui.screens.admin.flashcard

import com.englishlearningapp.data.remote.dto.flashcard.WordTypeDto

data class FlashcardFormUiState(
    val originalWord: String = "",
    val translatedWord: String = "",
    val explanation: String = "",
    val selectedWordTypeId: Int? = null,
    val wordTypes: List<WordTypeDto> = emptyList(),
    val isLoadingWordTypes: Boolean = false,
    val isLoadingFlashcard: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val successMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
) {
    val selectedWordType: WordTypeDto?
        get() = wordTypes.find { it.id == selectedWordTypeId }
}
