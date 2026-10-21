package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.question.AdminQuestionDto
import com.englishlearningapp.data.remote.dto.question.QuestionRequestDto
import com.englishlearningapp.data.remote.dto.question.QuestionTypeDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestionApi {
    @GET("api/admin/questions")
    suspend fun getQuestions(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("search") search: String? = null
    ): ApiResponse<List<AdminQuestionDto>>

    @GET("api/admin/questions/{id}")
    suspend fun getQuestion(@Path("id") id: Int): ApiResponse<AdminQuestionDto>

    @POST("api/admin/questions")
    suspend fun createQuestion(@Body request: QuestionRequestDto): ApiResponse<AdminQuestionDto>

    @PUT("api/admin/questions/{id}")
    suspend fun updateQuestion(
        @Path("id") id: Int,
        @Body request: QuestionRequestDto
    ): ApiResponse<AdminQuestionDto>

    @DELETE("api/admin/questions/{id}")
    suspend fun deleteQuestion(@Path("id") id: Int): ApiResponse<Unit>

    @GET("api/admin/question-types")
    suspend fun getQuestionTypes(): ApiResponse<List<QuestionTypeDto>>
}
