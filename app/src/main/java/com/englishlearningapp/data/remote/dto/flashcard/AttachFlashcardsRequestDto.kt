package com.englishlearningapp.data.remote.dto.flashcard

import com.google.gson.annotations.SerializedName

/**
 * Request body for attaching flashcards to a collection.
 */
data class AttachFlashcardsRequestDto(
    @SerializedName("flashcard_ids") val flashcardIds: List<Int>
)
