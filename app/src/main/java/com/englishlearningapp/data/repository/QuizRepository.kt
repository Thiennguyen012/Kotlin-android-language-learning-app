package com.englishlearningapp.data.repository

import com.englishlearningapp.data.remote.api.QuizApi
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestDetailDto
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestDto
import com.englishlearningapp.data.remote.dto.quiz.AttemptDetailDto
import com.englishlearningapp.data.remote.dto.quiz.QuestionDto
import com.englishlearningapp.data.remote.dto.quiz.SaveAnswerRequestDto
import com.englishlearningapp.data.remote.dto.quiz.StartAttemptDto
import com.englishlearningapp.data.remote.dto.quiz.AttemptHistoryDto
import com.englishlearningapp.data.remote.dto.common.MetaDto
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestRequestDto
import com.englishlearningapp.data.remote.dto.quiz.TestTypeDto
import com.englishlearningapp.core.network.BackendActionResult
import com.englishlearningapp.core.network.BackendResult
import com.englishlearningapp.core.network.requireBackendData
import com.englishlearningapp.core.network.requireBackendSuccess

/**
 * Repository for quiz/test operations.
 * Calls QuizApi and handles response validation.
 */
class QuizRepository(private val quizApi: QuizApi) {

    /**
     * Get all available tests.
     */
    suspend fun getTests(): List<CollectionTestDto> {
        val response = quizApi.getTests()
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    suspend fun getTests(
        page: Int,
        perPage: Int = 10,
        search: String? = null
    ): Pair<List<CollectionTestDto>, MetaDto?> {
        val response = quizApi.getTests(page, perPage, search?.takeIf { it.isNotBlank() })
        return response.requireData() to response.meta
    }

    /**
     * Get a single test with all its questions.
     */
    suspend fun getTestDetail(id: Int): CollectionTestDetailDto {
        val response = quizApi.getTestDetail(id)
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    suspend fun getTestTypes(): List<TestTypeDto> = quizApi.getTestTypes().requireData()

    suspend fun createTest(request: CollectionTestRequestDto): BackendResult<CollectionTestDetailDto> =
        quizApi.createTest(request).requireBackendData()

    suspend fun updateTest(id: Int, request: CollectionTestRequestDto): BackendResult<CollectionTestDetailDto> =
        quizApi.updateTest(id, request).requireBackendData()

    suspend fun deleteTest(id: Int): BackendActionResult =
        quizApi.deleteTest(id).requireBackendSuccess()

    suspend fun startAttempt(testId: Int): StartAttemptDto {
        val response = quizApi.startAttempt(testId)
        return response.requireData()
    }

    suspend fun getAttempt(attemptId: Int): AttemptDetailDto {
        val response = quizApi.getAttempt(attemptId)
        return response.requireData()
    }

    suspend fun getAttemptQuestions(attemptId: Int): List<QuestionDto> {
        val response = quizApi.getAttemptQuestions(attemptId)
        return response.requireData()
    }

    suspend fun saveAnswer(attemptId: Int, questionId: Int, userAnswer: Any) {
        val response = quizApi.saveAnswer(
            attemptId = attemptId,
            request = SaveAnswerRequestDto(questionId, userAnswer)
        )
        if (response.statusCode !in 200..299) {
            throw Exception(response.message)
        }
    }

    suspend fun submitAttempt(attemptId: Int) {
        val response = quizApi.submitAttempt(attemptId)
        if (response.statusCode !in 200..299) {
            throw Exception(response.message)
        }
    }

    suspend fun getMyAttempts(page: Int): Pair<List<AttemptHistoryDto>, MetaDto?> {
        val response = quizApi.getMyAttempts(page)
        val attempts = response.requireData()
        return attempts to response.meta
    }

    private fun <T> com.englishlearningapp.data.remote.dto.auth.ApiResponse<T>.requireData(): T {
        if (statusCode !in 200..299 || data == null) {
            throw Exception(message)
        }
        return data
    }
}
