package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDetailDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardRequestDto
import com.englishlearningapp.data.remote.dto.flashcard.WordTypeDto
import com.englishlearningapp.data.remote.dto.flashcard.AttachFlashcardsRequestDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Query

/**
 * Retrofit API interface for flashcard endpoints.
 */
interface FlashcardApi {

    @GET("api/admin/flashcard-collections")
    suspend fun getCollections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15
    ): ApiResponse<List<FlashcardCollectionDto>>

    @GET("api/admin/flashcard-collections/{id}")
    suspend fun getCollectionDetail(@Path("id") id: Int): ApiResponse<FlashcardCollectionDetailDto>

    // --- Flashcard Operations ---

    @GET("api/admin/flashcards")
    suspend fun getFlashcards(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("search") search: String? = null
    ): ApiResponse<List<FlashcardDto>>

    @GET("api/admin/flashcards/{id}")
    suspend fun getFlashcard(@Path("id") id: Int): ApiResponse<FlashcardDto>

    @POST("api/admin/flashcards")
    suspend fun createFlashcard(@Body request: FlashcardRequestDto): ApiResponse<FlashcardDto>

    @PUT("api/admin/flashcards/{id}")
    suspend fun updateFlashcard(
        @Path("id") id: Int,
        @Body request: FlashcardRequestDto
    ): ApiResponse<FlashcardDto>

    @DELETE("api/admin/flashcards/{id}")
    suspend fun deleteFlashcard(@Path("id") id: Int): ApiResponse<Unit>

    // --- Word Types / Test Types ---

    @GET("api/admin/word-types")
    suspend fun getWordTypes(): ApiResponse<List<WordTypeDto>>

    // --- Collection Attach Operations ---

    @POST("api/admin/flashcard-collections/{id}/attach")
    suspend fun attachFlashcardsToCollection(
        @Path("id") collectionId: Int,
        @Body request: AttachFlashcardsRequestDto
    ): ApiResponse<Unit>
}
