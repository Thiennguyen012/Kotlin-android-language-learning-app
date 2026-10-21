package com.englishlearningapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Edit Profile screen.
 */
data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val birthday: String = "",
    val address: String = "",
    val email: String = "",           // Read-only, displayed but not editable
    val avatarPath: String? = null,
    val avatarPreviewUri: String? = null,
    val avatarBytes: ByteArray? = null,
    val avatarFileName: String? = null,
    val avatarMimeType: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    // Validation errors (shown under each field)
    val nameError: String? = null,
    val phoneError: String? = null,
    val birthdayError: String? = null,
    val addressError: String? = null
)

/**
 * ViewModel for the Edit Profile screen.
 * Handles form state, validation, and saving.
 */
class EditProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    /**
     * Initialize form fields with existing user data.
     * Called from the screen when it receives the user data.
     */
    fun initializeForm(
        name: String,
        phone: String,
        birthday: String,
        address: String,
        email: String,
        avatarPath: String?
    ) {
        // Only initialize if the form is empty (avoid overwriting user edits)
        if (_uiState.value.email.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                name = name,
                phone = phone,
                birthday = birthday,
                address = address,
                email = email,
                avatarPath = avatarPath
            )
        }
    }

    // --- Field change handlers ---

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null, errorMessage = null)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, phoneError = null, errorMessage = null)
    }

    fun onBirthdayChange(birthday: String) {
        _uiState.value = _uiState.value.copy(birthday = birthday, birthdayError = null, errorMessage = null)
    }

    fun onAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(address = address, addressError = null, errorMessage = null)
    }

    fun onAvatarSelected(
        bytes: ByteArray,
        fileName: String,
        mimeType: String?,
        previewUri: String
    ) {
        _uiState.value = _uiState.value.copy(
            avatarBytes = bytes,
            avatarFileName = fileName,
            avatarMimeType = mimeType,
            avatarPreviewUri = previewUri,
            errorMessage = null
        )
    }

    /**
     * Validate all fields and save if valid.
     */
    fun saveProfile() {
        val current = _uiState.value

        // --- Validation ---
        var hasError = false
        var nameError: String? = null
        var phoneError: String? = null
        var birthdayError: String? = null
        var addressError: String? = null

        if (current.name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        }
        if (current.phone.isBlank()) {
            phoneError = "Phone cannot be empty"
            hasError = true
        }
        if (current.birthday.isBlank()) {
            birthdayError = "Birthday cannot be empty"
            hasError = true
        }
        if (current.address.isBlank()) {
            addressError = "Address cannot be empty"
            hasError = true
        }

        if (hasError) {
            _uiState.value = current.copy(
                nameError = nameError,
                phoneError = phoneError,
                birthdayError = birthdayError,
                addressError = addressError
            )
            return
        }

        // --- Save ---
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                profileRepository.updateProfile(
                    name = current.name.trim(),
                    phone = current.phone.trim(),
                    birthday = current.birthday.trim(),
                    address = current.address.trim(),
                    avatarBytes = current.avatarBytes,
                    avatarFileName = current.avatarFileName,
                    avatarMimeType = current.avatarMimeType
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    /**
     * Factory for creating EditProfileViewModel with dependencies.
     */
    class Factory(
        private val profileRepository: ProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
                return EditProfileViewModel(profileRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
