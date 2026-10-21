package com.englishlearningapp.ui.screens.admin.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.auth.PermissionDto
import com.englishlearningapp.data.remote.dto.auth.PermissionRequestDto
import com.englishlearningapp.data.repository.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PermissionFormUiState(
    val permissionName: String = "",
    val description: String = "",
    val validationErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class PermissionFormViewModel(
    private val repository: PermissionRepository,
    private val permissionId: Int?,
    initialPermission: PermissionDto?
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        PermissionFormUiState(
            permissionName = initialPermission?.permissionName.orEmpty(),
            description = initialPermission?.description.orEmpty()
        )
    )
    val uiState: StateFlow<PermissionFormUiState> = _uiState.asStateFlow()

    fun updatePermissionName(value: String) {
        _uiState.value = _uiState.value.copy(
            permissionName = value,
            validationErrors = _uiState.value.validationErrors - "permissionName"
        )
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value,
            validationErrors = _uiState.value.validationErrors - "description"
        )
    }

    fun savePermission() {
        if (_uiState.value.isSaving) return
        val errors = validate()
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val request = PermissionRequestDto(
                    permissionName = _uiState.value.permissionName.trim(),
                    description = _uiState.value.description.trim()
                )
                val result = if (permissionId == null) {
                    repository.createPermission(request)
                } else {
                    repository.updatePermission(permissionId, request)
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaveSuccess = true,
                    successMessage = result.message
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validate(): Map<String, String> = buildMap {
        if (_uiState.value.permissionName.isBlank()) {
            put("permissionName", "Permission name is required")
        }
        if (_uiState.value.description.isBlank()) {
            put("description", "Description is required")
        }
    }

    class Factory(
        private val repository: PermissionRepository,
        private val permissionId: Int?,
        private val initialPermission: PermissionDto?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PermissionFormViewModel::class.java)) {
                return PermissionFormViewModel(repository, permissionId, initialPermission) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
