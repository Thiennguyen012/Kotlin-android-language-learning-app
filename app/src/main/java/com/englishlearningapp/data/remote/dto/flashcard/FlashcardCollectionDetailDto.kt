package com.englishlearningapp.data.remote.dto.flashcard

import com.google.gson.annotations.SerializedName

/**
 * A flashcard collection detail as returned by the single-item endpoint.
 * Includes the list of flashcards in this collection.
 */
data class FlashcardCollectionDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("collection_name") val collectionName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("flashcards") val flashcards: List<FlashcardDto>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
