package com.englishlearningapp.data.remote.dto.speaking

import com.google.gson.annotations.SerializedName

data class PronunciationSentenceDto(
    @SerializedName("id") val id: Long,
    @SerializedName("text") val text: String,
    @SerializedName("level") val level: String,
    @SerializedName("dialect") val dialect: String,
    @SerializedName("topic") val topic: String? = null
)

data class SpeakingAssessmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("sentence_id") val sentenceId: Long? = null,
    @SerializedName("reference_text") val referenceText: String? = null,
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