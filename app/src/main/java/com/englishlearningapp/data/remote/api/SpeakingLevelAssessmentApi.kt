package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.speaking.CreateSpeakingLevelAssessmentRequest
import com.englishlearningapp.data.remote.dto.speaking.SpeakingLevelAssessmentDto
import com.englishlearningapp.data.remote.dto.speaking.SpeakingTopicDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SpeakingLevelAssessmentApi {
    @GET("api/admin/speaking-level-assessments/topics")
    suspend fun topics(): ApiResponse<List<SpeakingTopicDto>>

    @POST("api/admin/speaking-level-assessments")
    suspend fun create(
        @Body request: CreateSpeakingLevelAssessmentRequest
    ): ApiResponse<SpeakingLevelAssessmentDto>

    @Multipart
    @POST("api/admin/speaking-level-assessments/{uuid}/answer")
    suspend fun submitAnswer(
        @Path("uuid") uuid: String,
        @Part audio: MultipartBody.Part
    ): ApiResponse<SpeakingLevelAssessmentDto>

    @GET("api/admin/speaking-level-assessments/{uuid}")
    suspend fun get(@Path("uuid") uuid: String): ApiResponse<SpeakingLevelAssessmentDto>
}
