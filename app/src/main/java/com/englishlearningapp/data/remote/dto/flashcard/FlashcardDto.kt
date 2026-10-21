package com.englishlearningapp.data.remote.dto.flashcard

import com.google.gson.annotations.SerializedName

/**
 * A single flashcard with original (English) and translated words.
 */
data class FlashcardDto(
    @SerializedName("id") val id: Int,
    @SerializedName("original_word") val originalWord: String,
    @SerializedName("translated_word") val translatedWord: String,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("word_type") val wordType: WordTypeDto? = null
)
