package com.englishlearningapp.ui.screens.admin.role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.role.RoleDto
import com.englishlearningapp.data.repository.RoleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoleManagementUiState(
    val roles: List<RoleDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val deletingRole: RoleDto? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredRoles: List<RoleDto>
        get() {
            val query = searchQuery.trim()
            if (query.isBlank()) return roles
            return roles.filter { role ->
                role.roleName.contains(query, ignoreCase = true) ||
                    role.description.orEmpty().contains(query, ignoreCase = true)
            }
        }
}

class RoleManagementViewModel(
    private val repository: RoleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoleManagementUiState())
    val uiState: StateFlow<RoleManagementUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun updateSearchQuery(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }

    fun findRole(id: Int): RoleDto? = _uiState.value.roles.firstOrNull { it.id == id }

    fun requestDelete(role: RoleDto) {
        _uiState.value = _uiState.value.copy(deletingRole = role)
    }

    fun dismissDelete() {
        if (!_uiState.value.isDeleting) _uiState.value = _uiState.value.copy(deletingRole = null)
    }

    fun confirmDelete() {
        val role = _uiState.value.deletingRole ?: return
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                val result = repository.deleteRole(role.id)
                _uiState.value = _uiState.value.copy(
                    roles = _uiState.value.roles.filterNot { it.id == role.id },
                    deletingRole = null,
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
                    roles = repository.getRoles(),
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

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }

    class Factory(private val repository: RoleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoleManagementViewModel::class.java)) {
                return RoleManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
