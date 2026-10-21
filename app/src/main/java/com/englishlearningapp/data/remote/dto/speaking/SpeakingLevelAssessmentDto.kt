package com.englishlearningapp.data.remote.dto.speaking

import com.google.gson.annotations.SerializedName

data class SpeakingTopicDto(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String
)

data class CreateSpeakingLevelAssessmentRequest(
    @SerializedName("topic") val topic: String
)

data class SpeakingLevelAssessmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("question") val question: String,
    @SerializedName("status") val status: String,
    @SerializedName("preparation_seconds") val preparationSeconds: Int,
    @SerializedName("response_seconds") val responseSeconds: Int,
    @SerializedName("transcript") val transcript: String? = null,
    @SerializedName("result") val result: SpeakingLevelResultDto? = null,
    @SerializedName("error_code") val errorCode: String? = null
)

data class SpeakingLevelResultDto(
    @SerializedName("overall_score") val overallScore: Double,
    @SerializedName("cefr_level") val cefrLevel: String,
    @SerializedName("scores") val scores: SpeakingLevelScoresDto,
    @SerializedName("grammar_errors") val grammarErrors: List<GrammarErrorDto> = emptyList(),
    @SerializedName("strengths") val strengths: List<String> = emptyList(),
    @SerializedName("improvements") val improvements: List<String> = emptyList(),
    @SerializedName("upgraded_answer") val upgradedAnswer: String = ""
)

data class SpeakingLevelScoresDto(
    @SerializedName("pronunciation") val pronunciation: Double,
    @SerializedName("fluency") val fluency: Double,
    @SerializedName("grammar") val grammar: Double,
    @SerializedName("vocabulary") val vocabulary: Double,
    @SerializedName("coherence") val coherence: Double,
    @SerializedName("topic_relevance") val topicRelevance: Double
)

data class GrammarErrorDto(
    @SerializedName("original") val original: String,
    @SerializedName("corrected") val corrected: String,
    @SerializedName("explanation") val explanation: String
)
