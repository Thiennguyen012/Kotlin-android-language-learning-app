package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.speaking.PronunciationSentenceDto
import com.englishlearningapp.data.remote.dto.speaking.SpeakingAssessmentDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit endpoints theo đúng thứ tự luồng: lấy câu -> nộp audio -> đọc trạng thái. */
interface SpeakingAssessmentApi {
    @GET("api/admin/pronunciation-sentences/random")
    suspend fun randomSentence(
        @Query("exclude_id") excludeId: Long? = null
    ): ApiResponse<PronunciationSentenceDto>

    @Multipart
    @POST("api/admin/speaking-assessments")
    suspend fun submit(
        @Part("sentence_id") sentenceId: RequestBody?,
        @Part("reference_text") referenceText: RequestBody,
        @Part("dialect") dialect: RequestBody,
        @Part("idempotency_key") idempotencyKey: RequestBody,
        @Part audio: MultipartBody.Part
    ): ApiResponse<SpeakingAssessmentDto>

    @GET("api/admin/speaking-assessments/{uuid}")
    suspend fun get(@Path("uuid") uuid: String): ApiResponse<SpeakingAssessmentDto>
}
