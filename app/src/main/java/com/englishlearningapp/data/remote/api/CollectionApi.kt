package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.flashcard.CollectionRequestDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDetailDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.data.remote.dto.flashcard.AttachFlashcardsRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for admin flashcard collection management.
 */
interface CollectionApi {

    @GET("api/admin/flashcard-collections")
    suspend fun getCollections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): ApiResponse<List<FlashcardCollectionDto>>

    @GET("api/admin/flashcard-collections/{id}")
    suspend fun getCollectionDetail(@Path("id") id: Int): ApiResponse<FlashcardCollectionDetailDto>

    @POST("api/admin/flashcard-collections")
    suspend fun createCollection(@Body request: CollectionRequestDto): ApiResponse<FlashcardCollectionDetailDto>

    @PUT("api/admin/flashcard-collections/{id}")
    suspend fun updateCollection(
        @Path("id") id: Int,
        @Body request: CollectionRequestDto
    ): ApiResponse<FlashcardCollectionDetailDto>

    @DELETE("api/admin/flashcard-collections/{id}")
    suspend fun deleteCollection(@Path("id") id: Int): ApiResponse<Unit>

    @POST("api/admin/flashcard-collections/{id}/attach")
    suspend fun attachFlashcards(
        @Path("id") id: Int,
        @Body request: AttachFlashcardsRequestDto
    ): ApiResponse<Unit>

    @POST("api/admin/flashcard-collections/{id}/detach")
    suspend fun detachFlashcards(
        @Path("id") id: Int,
        @Body request: AttachFlashcardsRequestDto
    ): ApiResponse<Unit>
}
