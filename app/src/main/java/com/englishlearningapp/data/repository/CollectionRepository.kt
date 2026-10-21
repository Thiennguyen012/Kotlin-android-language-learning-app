package com.englishlearningapp.data.repository

import com.englishlearningapp.data.remote.api.CollectionApi
import com.englishlearningapp.data.remote.dto.flashcard.CollectionRequestDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDetailDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.data.remote.dto.flashcard.AttachFlashcardsRequestDto
import com.englishlearningapp.data.remote.dto.common.MetaDto
import com.englishlearningapp.core.network.BackendActionResult
import com.englishlearningapp.core.network.BackendResult
import com.englishlearningapp.core.network.requireBackendData
import com.englishlearningapp.core.network.requireBackendSuccess

/**
 * Repository for Admin flashcard collection management.
 */
class CollectionRepository(private val collectionApi: CollectionApi) {

    suspend fun getCollections(
        page: Int = 1,
        perPage: Int = 10
    ): Pair<List<FlashcardCollectionDto>, MetaDto?> {
        val response = collectionApi.getCollections(page, perPage)
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data to response.meta
    }

    suspend fun getCollectionDetail(id: Int): FlashcardCollectionDetailDto {
        val response = collectionApi.getCollectionDetail(id)
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    suspend fun createCollection(
        name: String,
        description: String?
    ): BackendResult<FlashcardCollectionDetailDto> = collectionApi.createCollection(
        CollectionRequestDto(name, description)
    ).requireBackendData()

    suspend fun updateCollection(
        id: Int,
        name: String,
        description: String?
    ): BackendResult<FlashcardCollectionDetailDto> = collectionApi.updateCollection(
        id,
        CollectionRequestDto(name, description)
    ).requireBackendData()

    suspend fun deleteCollection(id: Int): BackendActionResult =
        collectionApi.deleteCollection(id).requireBackendSuccess()

    suspend fun attachFlashcards(id: Int, flashcardIds: List<Int>): BackendActionResult =
        collectionApi.attachFlashcards(id, AttachFlashcardsRequestDto(flashcardIds))
            .requireBackendSuccess()

    suspend fun detachFlashcards(id: Int, flashcardIds: List<Int>): BackendActionResult =
        collectionApi.detachFlashcards(id, AttachFlashcardsRequestDto(flashcardIds))
            .requireBackendSuccess()
}
