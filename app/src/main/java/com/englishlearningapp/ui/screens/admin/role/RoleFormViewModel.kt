package com.englishlearningapp.ui.screens.admin.role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.auth.PermissionDto
import com.englishlearningapp.data.remote.dto.role.RoleDto
import com.englishlearningapp.data.remote.dto.role.RoleRequestDto
import com.englishlearningapp.data.repository.PermissionRepository
import com.englishlearningapp.data.repository.RoleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoleFormUiState(
    val roleName: String = "",
    val description: String = "",
    val permissions: List<PermissionDto> = emptyList(),
    val selectedPermissionIds: Set<Int> = emptySet(),
    val permissionSearchQuery: String = "",
    val isLoadingPermissions: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredPermissions: List<PermissionDto>
        get() {
            val query = permissionSearchQuery.trim()
            if (query.isBlank()) return permissions
            return permissions.filter { permission ->
                permission.permissionName.contains(query, ignoreCase = true) ||
                    permission.description.orEmpty().contains(query, ignoreCase = true)
            }
        }
}

class RoleFormViewModel(
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
    private val roleId: Int?,
    initialRole: RoleDto?
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RoleFormUiState(
            roleName = initialRole?.roleName.orEmpty(),
            description = initialRole?.description.orEmpty(),
            selectedPermissionIds = initialRole?.permissionIds.orEmpty().toSet()
        )
    )
    val uiState: StateFlow<RoleFormUiState> = _uiState.asStateFlow()

    init { loadPermissions() }

    fun updateRoleName(value: String) {
        _uiState.value = _uiState.value.copy(
            roleName = value,
            validationErrors = _uiState.value.validationErrors - "roleName"
        )
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value,
            validationErrors = _uiState.value.validationErrors - "description"
        )
    }

    fun updatePermissionSearch(value: String) {
        _uiState.value = _uiState.value.copy(permissionSearchQuery = value)
    }

    fun clearPermissionSearch() {
        _uiState.value = _uiState.value.copy(permissionSearchQuery = "")
    }

    fun togglePermission(permissionId: Int) {
        val selectedIds = _uiState.value.selectedPermissionIds.toMutableSet()
        if (!selectedIds.add(permissionId)) selectedIds.remove(permissionId)
        _uiState.value = _uiState.value.copy(
            selectedPermissionIds = selectedIds,
            validationErrors = _uiState.value.validationErrors - "permissions"
        )
    }

    fun saveRole() {
        if (_uiState.value.isSaving) return
        val errors = validate()
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val request = RoleRequestDto(
                    roleName = _uiState.value.roleName.trim(),
                    description = _uiState.value.description.trim(),
                    permissionIds = _uiState.value.selectedPermissionIds.sorted()
                )
                val result = if (roleId == null) {
                    roleRepository.createRole(request)
                } else {
                    roleRepository.updateRole(roleId, request)
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

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    private fun loadPermissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPermissions = true, errorMessage = null)
            try {
                _uiState.value = _uiState.value.copy(
                    permissions = permissionRepository.getPermissions(),
                    isLoadingPermissions = false
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPermissions = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    private fun validate(): Map<String, String> = buildMap {
        if (_uiState.value.roleName.isBlank()) put("roleName", "Role name is required")
        if (_uiState.value.description.isBlank()) put("description", "Description is required")
        if (_uiState.value.selectedPermissionIds.isEmpty()) {
            put("permissions", "Select at least one permission")
        }
    }

    class Factory(
        private val roleRepository: RoleRepository,
        private val permissionRepository: PermissionRepository,
        private val roleId: Int?,
        private val initialRole: RoleDto?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoleFormViewModel::class.java)) {
                return RoleFormViewModel(
                    roleRepository,
                    permissionRepository,
                    roleId,
                    initialRole
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
