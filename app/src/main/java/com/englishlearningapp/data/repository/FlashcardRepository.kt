package com.englishlearningapp.data.repository

import com.englishlearningapp.data.remote.api.FlashcardApi
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDetailDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardRequestDto
import com.englishlearningapp.data.remote.dto.flashcard.WordTypeDto
import com.englishlearningapp.data.remote.dto.flashcard.AttachFlashcardsRequestDto
import com.englishlearningapp.data.remote.dto.common.MetaDto
import com.englishlearningapp.core.network.BackendActionResult
import com.englishlearningapp.core.network.BackendResult
import com.englishlearningapp.core.network.requireBackendData
import com.englishlearningapp.core.network.requireBackendSuccess

/**
 * Repository for flashcard operations.
 * Calls FlashcardApi and handles response validation.
 */
class FlashcardRepository(private val flashcardApi: FlashcardApi) {

    /**
     * Get all flashcard collections.
     */
    suspend fun getCollections(): List<FlashcardCollectionDto> {
        val response = flashcardApi.getCollections()
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    suspend fun getAllCollections(): List<FlashcardCollectionDto> {
        val collections = mutableListOf<FlashcardCollectionDto>()
        var page = 1
        do {
            val response = flashcardApi.getCollections(page = page, perPage = 100)
            if (response.statusCode != 200 || response.data == null) throw Exception(response.message)
            collections += response.data
            val lastPage = response.meta?.lastPage ?: page
            page++
        } while (page <= lastPage)
        return collections.distinctBy { it.id }
    }

    /**
     * Get a single collection with all its flashcards.
     */
    suspend fun getCollectionDetail(id: Int): FlashcardCollectionDetailDto {
        val response = flashcardApi.getCollectionDetail(id)
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    // --- Flashcard Operations ---

    suspend fun getFlashcards(
        page: Int,
        perPage: Int = 10,
        search: String? = null
    ): Pair<List<FlashcardDto>, MetaDto?> {
        val response = flashcardApi.getFlashcards(page, perPage, search?.takeIf { it.isNotBlank() })
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data to response.meta
    }

    suspend fun getFlashcard(id: Int): FlashcardDto {
        val response = flashcardApi.getFlashcard(id)
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    suspend fun createFlashcard(
        originalWord: String,
        translatedWord: String,
        wordTypeId: Int,
        explanation: String?
    ): BackendResult<FlashcardDto> = flashcardApi.createFlashcard(
        FlashcardRequestDto(originalWord, translatedWord, wordTypeId, explanation)
    ).requireBackendData()

    suspend fun updateFlashcard(
        id: Int,
        originalWord: String,
        translatedWord: String,
        wordTypeId: Int,
        explanation: String?
    ): BackendResult<FlashcardDto> = flashcardApi.updateFlashcard(
        id,
        FlashcardRequestDto(originalWord, translatedWord, wordTypeId, explanation)
    ).requireBackendData()

    suspend fun deleteFlashcard(id: Int): BackendActionResult =
        flashcardApi.deleteFlashcard(id).requireBackendSuccess()

    // --- Word Types ---

    suspend fun getWordTypes(): List<WordTypeDto> {
        val response = flashcardApi.getWordTypes()
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    // --- Collection Attach Operations ---

    suspend fun attachToCollection(collectionId: Int, flashcardIds: List<Int>) {
        val response = flashcardApi.attachFlashcardsToCollection(collectionId, AttachFlashcardsRequestDto(flashcardIds))
        if (response.statusCode != 200) {
            throw Exception(response.message)
        }
    }
}
