package com.englishlearningapp.ui.screens.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.role.RoleDto
import com.englishlearningapp.data.repository.RoleRepository
import com.englishlearningapp.data.repository.UserFormPayload
import com.englishlearningapp.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserFormUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val birthday: String = "",
    val address: String = "",
    val status: Int = 1,
    val isSuperAdmin: Boolean = false,
    val roles: List<RoleDto> = emptyList(),
    val selectedRoleIds: Set<Int> = emptySet(),
    val avatarBytes: ByteArray? = null,
    val avatarFileName: String? = null,
    val avatarMimeType: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class UserFormViewModel(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userId: Int?
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserFormUiState())
    val uiState: StateFlow<UserFormUiState> = _uiState.asStateFlow()

    init { loadForm() }

    fun updateName(value: String) = update { copy(name = value, validationErrors = validationErrors - "name") }
    fun updateEmail(value: String) = update { copy(email = value, validationErrors = validationErrors - "email") }
    fun updatePassword(value: String) = update { copy(password = value, validationErrors = validationErrors - "password") }
    fun updatePhone(value: String) = update { copy(phone = value) }
    fun updateBirthday(value: String) = update { copy(birthday = value) }
    fun updateAddress(value: String) = update { copy(address = value) }
    fun updateStatus(active: Boolean) = update { copy(status = if (active) 1 else 0) }
    fun updateSuperAdmin(value: Boolean) = update { copy(isSuperAdmin = value) }
    fun setAvatar(bytes: ByteArray, fileName: String, mimeType: String?) = update {
        copy(avatarBytes = bytes, avatarFileName = fileName, avatarMimeType = mimeType)
    }

    fun toggleRole(roleId: Int) {
        val ids = _uiState.value.selectedRoleIds.toMutableSet()
        if (!ids.add(roleId)) ids.remove(roleId)
        update { copy(selectedRoleIds = ids) }
    }

    fun saveUser() {
        if (_uiState.value.isSaving) return
        val errors = validate()
        if (errors.isNotEmpty()) {
            update { copy(validationErrors = errors) }
            return
        }
        viewModelScope.launch {
            update { copy(isSaving = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val payload = UserFormPayload(
                    name = state.name.trim(),
                    email = state.email.trim(),
                    password = state.password,
                    phone = state.phone.trim(),
                    birthday = state.birthday.trim(),
                    address = state.address.trim(),
                    status = state.status,
                    isSuperAdmin = state.isSuperAdmin,
                    roleIds = state.selectedRoleIds.sorted(),
                    avatarBytes = state.avatarBytes,
                    avatarFileName = state.avatarFileName,
                    avatarMimeType = state.avatarMimeType
                )
                val result = if (userId == null) {
                    userRepository.createUser(payload)
                } else {
                    userRepository.updateUser(userId, payload)
                }
                update {
                    copy(isSaving = false, isSaveSuccess = true, successMessage = result.message)
                }
            } catch (exception: Exception) {
                update { copy(isSaving = false, errorMessage = exception.toBackendMessage()) }
            }
        }
    }

    fun clearError() = update { copy(errorMessage = null) }

    private fun loadForm() {
        viewModelScope.launch {
            update { copy(isLoading = true, errorMessage = null) }
            try {
                val rolesRequest = async { roleRepository.getRoles() }
                val userRequest = userId?.let { id -> async { userRepository.getUser(id) } }
                val roles = rolesRequest.await()
                val user = userRequest?.await()
                update {
                    copy(
                        name = user?.name.orEmpty(),
                        email = user?.email.orEmpty(),
                        phone = user?.phone.orEmpty(),
                        birthday = user?.birthday.orEmpty().substringBefore("T"),
                        address = user?.address.orEmpty(),
                        status = user?.status ?: 1,
                        isSuperAdmin = user?.isSuperAdmin ?: false,
                        roles = roles,
                        selectedRoleIds = user?.roleIds.orEmpty().toSet(),
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                update { copy(isLoading = false, errorMessage = exception.toBackendMessage()) }
            }
        }
    }

    private fun validate(): Map<String, String> = buildMap {
        if (_uiState.value.name.isBlank()) put("name", "Name is required")
        if (_uiState.value.email.isBlank()) put("email", "Email is required")
        if (userId == null && _uiState.value.password.isBlank()) put("password", "Password is required")
    }

    private fun update(transform: UserFormUiState.() -> UserFormUiState) {
        _uiState.value = _uiState.value.transform()
    }

    class Factory(
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository,
        private val userId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserFormViewModel::class.java)) {
                return UserFormViewModel(userRepository, roleRepository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
