package com.englishlearningapp.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the flashcard collection list screen.
 */
data class CollectionListUiState(
    val collections: List<FlashcardCollectionDto> = emptyList(),
    val selectedFilter: CollectionFilter = CollectionFilter.NEWEST,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class CollectionFilter {
    NEWEST,
    OLDEST,
    NAME_ASCENDING
}

/**
 * ViewModel for displaying the list of flashcard collections.
 */
class FlashcardCollectionListViewModel(
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private var allCollections: List<FlashcardCollectionDto> = emptyList()

    private val _uiState = MutableStateFlow(CollectionListUiState())
    val uiState: StateFlow<CollectionListUiState> = _uiState.asStateFlow()

    init {
        loadCollections()
    }

    /**
     * Load all flashcard collections from the API.
     */
    fun loadCollections() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val collections = flashcardRepository.getCollections()
                allCollections = collections
                _uiState.value = _uiState.value.copy(
                    collections = filterCollections(collections, _uiState.value.selectedFilter),
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

    fun selectFilter(filter: CollectionFilter) {
        _uiState.value = _uiState.value.copy(
            collections = filterCollections(allCollections, filter),
            selectedFilter = filter
        )
    }

    private fun filterCollections(
        collections: List<FlashcardCollectionDto>,
        filter: CollectionFilter
    ): List<FlashcardCollectionDto> {
        return when (filter) {
            CollectionFilter.NEWEST -> collections.sortedByDescending { it.updatedAt }
            CollectionFilter.OLDEST -> collections.sortedBy { it.updatedAt }
            CollectionFilter.NAME_ASCENDING -> collections.sortedBy { it.collectionName.lowercase() }
        }
    }

    /**
     * Factory for creating this ViewModel with dependencies.
     */
    class Factory(
        private val flashcardRepository: FlashcardRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FlashcardCollectionListViewModel::class.java)) {
                return FlashcardCollectionListViewModel(flashcardRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
