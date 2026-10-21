package com.englishlearningapp.ui.screens.admin.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.auth.PermissionDto
import com.englishlearningapp.data.repository.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PermissionManagementUiState(
    val permissions: List<PermissionDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val deletingPermission: PermissionDto? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredPermissions: List<PermissionDto>
        get() {
            val query = searchQuery.trim()
            if (query.isBlank()) return permissions
            return permissions.filter { permission ->
                permission.permissionName.contains(query, ignoreCase = true) ||
                    permission.description.orEmpty().contains(query, ignoreCase = true)
            }
        }
}

class PermissionManagementViewModel(
    private val repository: PermissionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PermissionManagementUiState())
    val uiState: StateFlow<PermissionManagementUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun updateSearchQuery(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }

    fun findPermission(id: Int): PermissionDto? =
        _uiState.value.permissions.firstOrNull { it.id == id }

    fun requestDelete(permission: PermissionDto) {
        _uiState.value = _uiState.value.copy(deletingPermission = permission)
    }

    fun dismissDelete() {
        if (!_uiState.value.isDeleting) {
            _uiState.value = _uiState.value.copy(deletingPermission = null)
        }
    }

    fun confirmDelete() {
        val permission = _uiState.value.deletingPermission ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                val result = repository.deletePermission(permission.id)
                _uiState.value = _uiState.value.copy(
                    permissions = _uiState.value.permissions.filterNot { it.id == permission.id },
                    deletingPermission = null,
                    isDeleting = false,
                    successMessage = result.message
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _uiState.value = _uiState.value.copy(
                    permissions = repository.getPermissions(),
                    isLoading = false
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    class Factory(
        private val repository: PermissionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PermissionManagementViewModel::class.java)) {
                return PermissionManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
