package com.englishlearningapp.data.remote.dto.quiz

import com.google.gson.annotations.SerializedName

/**
 * Simplified collection info embedded in a collection test response.
 * Separate from FlashcardCollectionDto to keep quiz and flashcard packages independent.
 */
data class CollectionSummaryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("collection_name") val collectionName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
