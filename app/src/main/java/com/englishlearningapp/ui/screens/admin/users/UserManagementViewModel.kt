package com.englishlearningapp.ui.screens.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.auth.UserDto
import com.englishlearningapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val users: List<UserDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val deletingUser: UserDto? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredUsers: List<UserDto>
        get() {
            val query = searchQuery.trim()
            if (query.isBlank()) return users
            return users.filter { user ->
                user.name.contains(query, true) ||
                    user.email.contains(query, true) ||
                    user.phone.orEmpty().contains(query, true)
            }
        }
}

class UserManagementViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun updateSearchQuery(value: String) { _uiState.value = _uiState.value.copy(searchQuery = value) }
    fun clearSearch() { _uiState.value = _uiState.value.copy(searchQuery = "") }
    fun requestDelete(user: UserDto) { _uiState.value = _uiState.value.copy(deletingUser = user) }
    fun dismissDelete() {
        if (!_uiState.value.isDeleting) _uiState.value = _uiState.value.copy(deletingUser = null)
    }

    fun confirmDelete() {
        val user = _uiState.value.deletingUser ?: return
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                val result = repository.deleteUser(user.id)
                _uiState.value = _uiState.value.copy(
                    users = _uiState.value.users.filterNot { it.id == user.id },
                    deletingUser = null,
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
                    users = repository.getUsers(),
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

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
                return UserManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
