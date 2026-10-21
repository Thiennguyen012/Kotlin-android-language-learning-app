package com.englishlearningapp.ui.screens.admin.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateEditCollectionUiState(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val saveSuccess: Boolean = false
)

class CreateEditCollectionViewModel(
    private val collectionRepository: CollectionRepository,
    private val collectionId: Int? = null // Null for Create, non-null for Edit
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditCollectionUiState())
    val uiState: StateFlow<CreateEditCollectionUiState> = _uiState.asStateFlow()

    init {
        // If editing, load existing data
        if (collectionId != null) {
            loadExistingCollection(collectionId)
        }
    }

    private fun loadExistingCollection(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val detail = collectionRepository.getCollectionDetail(id)
                _uiState.value = _uiState.value.copy(
                    name = detail.collectionName,
                    description = detail.description ?: "",
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

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null, errorMessage = null)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description, errorMessage = null)
    }

    fun saveCollection() {
        val current = _uiState.value

        if (current.name.isBlank()) {
            _uiState.value = current.copy(nameError = "Collection Name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true, errorMessage = null)
            try {
                val result = if (collectionId == null) {
                    // Create new
                    collectionRepository.createCollection(
                        name = current.name.trim(),
                        description = current.description.trim().takeIf { it.isNotEmpty() }
                    )
                } else {
                    // Update existing
                    collectionRepository.updateCollection(
                        id = collectionId,
                        name = current.name.trim(),
                        description = current.description.trim().takeIf { it.isNotEmpty() }
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    successMessage = result.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    class Factory(
        private val collectionRepository: CollectionRepository,
        private val collectionId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateEditCollectionViewModel::class.java)) {
                return CreateEditCollectionViewModel(collectionRepository, collectionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
