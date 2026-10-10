package com.vu.englishlearningapp.data.remote.api

import com.vu.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.vu.englishlearningapp.data.remote.dto.speaking.SpeakingAssessmentDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SpeakingAssessmentApi {
    @Multipart
    @POST("api/admin/speaking-assessments")
    suspend fun submit(
        @Part("reference_text") referenceText: RequestBody,
        @Part("dialect") dialect: RequestBody,
        @Part("idempotency_key") idempotencyKey: RequestBody,
        @Part audio: MultipartBody.Part
    ): ApiResponse<SpeakingAssessmentDto>

    @GET("api/admin/speaking-assessments/{uuid}")
    suspend fun get(@Path("uuid") uuid: String): ApiResponse<SpeakingAssessmentDto>
}
