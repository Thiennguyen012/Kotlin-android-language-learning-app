package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestDetailDto
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestDto
import com.englishlearningapp.data.remote.dto.quiz.AttemptDetailDto
import com.englishlearningapp.data.remote.dto.quiz.AttemptDto
import com.englishlearningapp.data.remote.dto.quiz.QuestionDto
import com.englishlearningapp.data.remote.dto.quiz.SaveAnswerRequestDto
import com.englishlearningapp.data.remote.dto.quiz.StartAttemptDto
import com.englishlearningapp.data.remote.dto.quiz.AttemptHistoryDto
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestRequestDto
import com.englishlearningapp.data.remote.dto.quiz.TestTypeDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT

/**
 * Retrofit API interface for quiz/test endpoints.
 */
interface QuizApi {

    @GET("api/admin/collection-tests")
    suspend fun getTests(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("search") search: String? = null
    ): ApiResponse<List<CollectionTestDto>>

    @GET("api/admin/collection-tests/{id}")
    suspend fun getTestDetail(@Path("id") id: Int): ApiResponse<CollectionTestDetailDto>

    @POST("api/admin/collection-tests")
    suspend fun createTest(@Body request: CollectionTestRequestDto): ApiResponse<CollectionTestDetailDto>

    @PUT("api/admin/collection-tests/{id}")
    suspend fun updateTest(
        @Path("id") id: Int,
        @Body request: CollectionTestRequestDto
    ): ApiResponse<CollectionTestDetailDto>

    @DELETE("api/admin/collection-tests/{id}")
    suspend fun deleteTest(@Path("id") id: Int): ApiResponse<Unit>

    @GET("api/admin/test-types")
    suspend fun getTestTypes(): ApiResponse<List<TestTypeDto>>

    @POST("api/admin/tests/{id}/start")
    suspend fun startAttempt(@Path("id") testId: Int): ApiResponse<StartAttemptDto>

    @GET("api/admin/attempts/{id}")
    suspend fun getAttempt(@Path("id") attemptId: Int): ApiResponse<AttemptDetailDto>

    @GET("api/admin/attempts/{id}/questions")
    suspend fun getAttemptQuestions(@Path("id") attemptId: Int): ApiResponse<List<QuestionDto>>

    @POST("api/admin/attempts/{id}/answers")
    suspend fun saveAnswer(
        @Path("id") attemptId: Int,
        @Body request: SaveAnswerRequestDto
    ): ApiResponse<Any>

    @POST("api/admin/attempts/{id}/submit")
    suspend fun submitAttempt(@Path("id") attemptId: Int): ApiResponse<AttemptDto>

    @GET("api/admin/my-attempts")
    suspend fun getMyAttempts(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 15
    ): ApiResponse<List<AttemptHistoryDto>>
}
