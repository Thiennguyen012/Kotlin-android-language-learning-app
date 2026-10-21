package com.englishlearningapp.data.remote.dto.quiz

import com.google.gson.annotations.SerializedName

/**
 * A collection test (quiz) as returned by the list endpoint.
 * Includes nested test_type and collection objects.
 */
data class CollectionTestDto(
    @SerializedName("id") val id: Int,
    @SerializedName("test_type_id") val testTypeId: Int,
    @SerializedName("collection_id") val collectionId: Int?,
    @SerializedName("test_name") val testName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("finished_at") val finishedAt: String?,
    @SerializedName("test_type") val testType: TestTypeDto,
    @SerializedName("collection") val collection: CollectionSummaryDto?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
