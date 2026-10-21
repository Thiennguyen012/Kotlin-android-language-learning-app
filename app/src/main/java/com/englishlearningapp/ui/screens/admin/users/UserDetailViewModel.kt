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

data class UserDetailUiState(
    val user: UserDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserDetailViewModel(
    private val repository: UserRepository,
    private val userId: Int
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _uiState.value = UserDetailUiState(user = repository.getUser(userId))
            } catch (exception: Exception) {
                _uiState.value = UserDetailUiState(errorMessage = exception.toBackendMessage())
            }
        }
    }

    class Factory(
        private val repository: UserRepository,
        private val userId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserDetailViewModel::class.java)) {
                return UserDetailViewModel(repository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
