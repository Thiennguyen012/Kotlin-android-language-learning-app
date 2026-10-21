package com.englishlearningapp.data.remote.dto.quiz

import com.google.gson.annotations.SerializedName

/**
 * A collection test detail as returned by the single-item endpoint.
 * Includes the list of questions with their question_data.
 */
data class CollectionTestDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("test_type_id") val testTypeId: Int,
    @SerializedName("collection_id") val collectionId: Int,
    @SerializedName("test_name") val testName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("finished_at") val finishedAt: String?,
    @SerializedName("question_ids") val questionIds: List<Int>,
    @SerializedName("questions") val questions: List<QuestionDto>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
