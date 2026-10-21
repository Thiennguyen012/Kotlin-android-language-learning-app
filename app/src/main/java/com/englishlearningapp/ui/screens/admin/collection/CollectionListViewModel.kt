package com.englishlearningapp.ui.screens.admin.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CollectionListUiState(
    val collections: List<FlashcardCollectionDto> = emptyList(),
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val total: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class CollectionListViewModel(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionListUiState())
    val uiState: StateFlow<CollectionListUiState> = _uiState.asStateFlow()

    init {
        loadCollections()
    }

    fun loadCollections(page: Int = _uiState.value.currentPage) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val (collections, meta) = collectionRepository.getCollections(page)
                _uiState.value = _uiState.value.copy(
                    collections = collections,
                    currentPage = meta?.currentPage ?: page,
                    lastPage = meta?.lastPage ?: 1,
                    total = meta?.total ?: collections.size,
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

    fun refreshCollections() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            try {
                val currentPage = _uiState.value.currentPage
                val (collections, meta) = collectionRepository.getCollections(currentPage)
                _uiState.value = _uiState.value.copy(
                    collections = collections,
                    currentPage = meta?.currentPage ?: currentPage,
                    lastPage = meta?.lastPage ?: 1,
                    total = meta?.total ?: collections.size,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun previousPage() {
        val page = _uiState.value.currentPage
        if (page > 1) loadCollections(page - 1)
    }

    fun nextPage() {
        val state = _uiState.value
        if (state.currentPage < state.lastPage) loadCollections(state.currentPage + 1)
    }

    class Factory(
        private val collectionRepository: CollectionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CollectionListViewModel::class.java)) {
                return CollectionListViewModel(collectionRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
