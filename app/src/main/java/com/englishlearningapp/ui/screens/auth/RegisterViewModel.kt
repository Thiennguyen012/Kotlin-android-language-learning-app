package com.englishlearningapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.auth.RegisterRequest
import com.englishlearningapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val birthday: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = updateState { copy(name = value) }
    fun onEmailChange(value: String) = updateState { copy(email = value) }
    fun onPhoneChange(value: String) = updateState { copy(phone = value) }
    fun onPasswordChange(value: String) = updateState { copy(password = value) }
    fun onPasswordConfirmationChange(value: String) =
        updateState { copy(passwordConfirmation = value) }
    fun onBirthdayChange(value: String) = updateState { copy(birthday = value) }
    fun onAddressChange(value: String) = updateState { copy(address = value) }

    fun register() {
        val state = _uiState.value
        val validationMessage = validate(state)
        if (validationMessage != null) {
            _uiState.value = state.copy(errorMessage = validationMessage)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                val result = authRepository.register(
                    RegisterRequest(
                        name = state.name.trim(),
                        email = state.email.trim(),
                        phone = state.phone.trim(),
                        password = state.password,
                        passwordConfirmation = state.passwordConfirmation,
                        birthday = state.birthday.trim(),
                        address = state.address.trim()
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = result.message
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toBackendMessage()
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun updateState(transform: RegisterUiState.() -> RegisterUiState) {
        _uiState.value = _uiState.value.transform().copy(errorMessage = null)
    }

    private fun validate(state: RegisterUiState): String? = when {
        state.name.isBlank() || state.email.isBlank() || state.phone.isBlank() ||
            state.password.isBlank() || state.passwordConfirmation.isBlank() ||
            state.birthday.isBlank() || state.address.isBlank() -> "Please complete all fields"
        !state.email.contains("@") -> "Please enter a valid email address"
        state.password != state.passwordConfirmation -> "Password confirmation does not match"
        else -> null
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
