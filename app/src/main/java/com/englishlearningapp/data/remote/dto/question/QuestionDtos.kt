package com.englishlearningapp.data.remote.dto.question

import com.google.gson.JsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName

data class AdminQuestionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("question_type") val questionType: QuestionTypeDto?,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("question_data") val questionData: JsonElement,
    @SerializedName("flashcard_reference_ids") val flashcardReferenceIds: List<Int>? = null,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    fun getQuestionDataObject(): JsonObject {
        return when {
            questionData.isJsonObject -> questionData.asJsonObject
            questionData.isJsonPrimitive && questionData.asJsonPrimitive.isString -> {
                runCatching {
                    JsonParser.parseString(questionData.asString).asJsonObject
                }.getOrDefault(JsonObject())
            }
            else -> JsonObject()
        }
    }
}

data class QuestionTypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("question_type_name") val name: String,
    @SerializedName("keyword") val keyword: String,
    @SerializedName("description") val description: String?
)

data class QuestionRequestDto(
    @SerializedName("question_type_id") val questionTypeId: Int,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("question_data") val questionData: JsonObject,
    @SerializedName("flashcard_reference_ids") val flashcardReferenceIds: List<Int>? = null
)
