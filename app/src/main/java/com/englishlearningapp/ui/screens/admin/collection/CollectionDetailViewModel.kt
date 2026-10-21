package com.englishlearningapp.ui.screens.admin.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDetailDto
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardDto
import com.englishlearningapp.data.repository.CollectionRepository
import com.englishlearningapp.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collection: FlashcardCollectionDetailDto? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isLoadingAvailableFlashcards: Boolean = false,
    val isUpdatingFlashcards: Boolean = false,
    val availableFlashcards: List<FlashcardDto> = emptyList(),
    val selectedFlashcardIds: Set<Int> = emptySet(),
    val isDeleteSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CollectionDetailViewModel(
    private val collectionRepository: CollectionRepository,
    private val flashcardRepository: FlashcardRepository,
    private val collectionId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()

    init {
        loadCollectionDetail()
    }

    fun loadCollectionDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val detail = collectionRepository.getCollectionDetail(collectionId)
                _uiState.value = _uiState.value.copy(
                    collection = detail,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    fun deleteCollection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                val result = collectionRepository.deleteCollection(collectionId)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    isDeleteSuccess = true,
                    successMessage = result.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    fun loadAvailableFlashcards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingAvailableFlashcards = true,
                errorMessage = null,
                selectedFlashcardIds = emptySet()
            )
            try {
                val flashcards = mutableListOf<FlashcardDto>()
                var page = 1
                var lastPage: Int
                do {
                    val (items, meta) = flashcardRepository.getFlashcards(page, perPage = 100)
                    flashcards += items
                    lastPage = meta?.lastPage ?: page
                    page++
                } while (page <= lastPage)
                val attachedIds = _uiState.value.collection?.flashcards.orEmpty().map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(
                    isLoadingAvailableFlashcards = false,
                    availableFlashcards = flashcards.distinctBy { it.id }
                        .filterNot { it.id in attachedIds }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingAvailableFlashcards = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    fun toggleFlashcardSelection(id: Int) {
        val selectedIds = _uiState.value.selectedFlashcardIds.toMutableSet()
        if (!selectedIds.add(id)) selectedIds.remove(id)
        _uiState.value = _uiState.value.copy(selectedFlashcardIds = selectedIds)
    }

    fun attachSelectedFlashcards(onSuccess: () -> Unit) {
        val ids = _uiState.value.selectedFlashcardIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingFlashcards = true, errorMessage = null)
            try {
                val result = collectionRepository.attachFlashcards(collectionId, ids)
                _uiState.value = _uiState.value.copy(
                    isUpdatingFlashcards = false,
                    successMessage = result.message,
                    selectedFlashcardIds = emptySet()
                )
                loadCollectionDetail()
                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingFlashcards = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun detachFlashcard(flashcardId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingFlashcards = true, errorMessage = null)
            try {
                val result = collectionRepository.detachFlashcards(collectionId, listOf(flashcardId))
                _uiState.value = _uiState.value.copy(
                    isUpdatingFlashcards = false,
                    successMessage = result.message
                )
                loadCollectionDetail()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingFlashcards = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    class Factory(
        private val collectionRepository: CollectionRepository,
        private val flashcardRepository: FlashcardRepository,
        private val collectionId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CollectionDetailViewModel::class.java)) {
                return CollectionDetailViewModel(collectionRepository, flashcardRepository, collectionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
