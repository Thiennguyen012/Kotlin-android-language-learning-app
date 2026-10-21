package com.englishlearningapp.data.remote.dto.flashcard

import com.google.gson.annotations.SerializedName

/**
 * Request body for creating or updating a flashcard collection.
 */
data class CollectionRequestDto(
    @SerializedName("collection_name") val collectionName: String,
    @SerializedName("description") val description: String?
)
