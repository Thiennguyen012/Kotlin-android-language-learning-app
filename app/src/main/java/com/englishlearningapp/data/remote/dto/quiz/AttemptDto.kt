package com.englishlearningapp.data.remote.dto.quiz

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName

data class StartAttemptDto(
    @SerializedName("attempt_id") val attemptId: Int,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("remaining_seconds") val remainingSeconds: Int? = null
)

data class AttemptDetailDto(
    @SerializedName("attempt") val attempt: AttemptDto,
    @SerializedName("remaining_seconds") val remainingSeconds: Int
)

data class AttemptDto(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("correct_count") val correctCount: Int,
    @SerializedName("total_score") val totalScore: Int,
    @SerializedName("started_time") val startedTime: String?,
    @SerializedName("finished_time") val finishedTime: String?,
    @SerializedName("total_time") val totalTime: String?,
    @SerializedName("expired_at") val expiresAt: String?,
    @SerializedName("collection_test") val collectionTest: AttemptTestDto?,
    @SerializedName("questions") val questions: List<AttemptQuestionDto> = emptyList()
)

data class AttemptTestDto(
    @SerializedName("id") val id: Int,
    @SerializedName("test_name") val testName: String,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("duration") val duration: Int
)

data class AttemptQuestionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("question_data") val questionData: JsonElement,
    @SerializedName("answer") val answer: AttemptAnswerDto?
) {
    fun getCorrectAnswer(): String {
        val data = questionData.toQuestionDataObject()
        data.get("answer")?.let { return it.asString }
        data.getAsJsonArray("pairs")?.let { pairs ->
            return pairs.joinToString(", ") { element ->
                val pair = element.asJsonObject
                "${pair.get("left").asString} → ${pair.get("right").asString}"
            }
        }
        val correct = data.get("correct") ?: return ""
        if (data.has("options") && correct.isJsonPrimitive && correct.asJsonPrimitive.isNumber) {
            return data.getAsJsonArray("options")?.get(correct.asInt)?.asString ?: correct.asString
        }
        return if (correct.isJsonPrimitive) correct.asString else correct.toString()
    }

    fun formatUserAnswer(userAnswer: String?): String {
        if (userAnswer.isNullOrBlank()) return "(no answer)"
        val data = questionData.toQuestionDataObject()
        if (data.has("options")) {
            return userAnswer.toIntOrNull()?.let { index ->
                data.getAsJsonArray("options")?.get(index)?.asString
            } ?: userAnswer
        }
        if (data.has("pairs")) {
            return runCatching {
                JsonParser.parseString(userAnswer).asJsonArray.joinToString(", ") { element ->
                    val pair = element.asJsonObject
                    "${pair.get("left").asString} → ${pair.get("right").asString}"
                }
            }.getOrDefault(userAnswer)
        }
        if (data.get("correct")?.asJsonPrimitive?.isBoolean == true) {
            return userAnswer.replaceFirstChar { it.uppercase() }
        }
        return userAnswer
    }
}

data class AttemptAnswerDto(
    @SerializedName("user_answer") val userAnswer: String,
    @SerializedName("is_correct") val isCorrect: Boolean
)

data class SaveAnswerRequestDto(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("user_answer") val userAnswer: Any
)

data class AttemptHistoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("collection_test") val collectionTest: AttemptTestDto?,
    @SerializedName("result") val result: AttemptHistoryResultDto,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("submitted_at") val submittedAt: String?,
    @SerializedName("total_time") val totalTime: String?
)

data class AttemptHistoryResultDto(
    @SerializedName("correct_count") val correctCount: Int,
    @SerializedName("total_score") val totalScore: Int
)
