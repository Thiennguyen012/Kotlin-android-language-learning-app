package com.englishlearningapp.data.remote.dto.flashcard

import com.google.gson.annotations.SerializedName

/**
 * Request body for creating or updating a flashcard.
 */
data class FlashcardRequestDto(
    @SerializedName("original_word") val originalWord: String,
    @SerializedName("translated_word") val translatedWord: String,
    @SerializedName("word_type_id") val wordTypeId: Int,
    @SerializedName("explanation") val explanation: String? = null
)
