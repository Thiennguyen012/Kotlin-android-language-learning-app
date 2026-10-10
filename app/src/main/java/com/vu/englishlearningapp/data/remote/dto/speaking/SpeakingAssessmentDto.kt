package com.vu.englishlearningapp.data.remote.dto.speaking

import com.google.gson.annotations.SerializedName

data class SpeakingAssessmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("transcript") val transcript: String? = null,
    @SerializedName("result") val result: SpeakingResultDto? = null,
    @SerializedName("error_code") val errorCode: String? = null
)

data class SpeakingResultDto(
    @SerializedName("text_score") val textScore: PronunciationScoreDto? = null
)

data class PronunciationScoreDto(
    @SerializedName("score") val score: Double? = null,
    @SerializedName("differences") val differences: PronunciationDifferencesDto? = null
)

data class PronunciationDifferencesDto(
    @SerializedName("errors") val errors: List<PronunciationWordErrorDto> = emptyList()
)

data class PronunciationWordErrorDto(
    @SerializedName("word") val word: String? = null,
    @SerializedName("expected") val expected: String? = null,
    @SerializedName("actual") val actual: String? = null
)
