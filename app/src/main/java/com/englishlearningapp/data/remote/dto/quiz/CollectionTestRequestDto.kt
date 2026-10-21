package com.englishlearningapp.data.remote.dto.quiz

import com.google.gson.annotations.SerializedName

data class CollectionTestRequestDto(
    @SerializedName("test_type_id") val testTypeId: Int,
    @SerializedName("collection_id") val collectionId: Int,
    @SerializedName("test_name") val testName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("duration") val duration: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("finished_at") val finishedAt: String,
    @SerializedName("question_ids") val questionIds: List<Int>
)
