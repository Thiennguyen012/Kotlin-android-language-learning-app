package com.englishlearningapp.core.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.auth.UserDto
import com.englishlearningapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PermissionUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val errorMessage: String? = null
)

class PermissionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    fun loadPermissions(forceRefresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        if (!forceRefresh && _uiState.value.user != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = authRepository.getCurrentUser()
                _uiState.value = PermissionUiState(user = user)
            } catch (exception: Exception) {
                _uiState.value = PermissionUiState(errorMessage = exception.toBackendMessage())
            }
        }
    }

    fun hasPermission(permissionName: String): Boolean {
        return PermissionHelper(_uiState.value.user).checkPermission(permissionName)
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
                return PermissionViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
