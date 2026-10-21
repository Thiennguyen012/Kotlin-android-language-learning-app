package com.englishlearningapp.data.remote.dto.flashcard

import com.google.gson.annotations.SerializedName

/**
 * A flashcard collection as returned by the list endpoint.
 * Does NOT include the individual flashcards.
 */
data class FlashcardCollectionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("collection_name") val collectionName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
