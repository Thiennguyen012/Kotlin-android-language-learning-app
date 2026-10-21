package com.englishlearningapp.data.repository

import com.englishlearningapp.core.session.TokenManager
import com.englishlearningapp.data.remote.api.AuthApi
import com.englishlearningapp.data.remote.dto.auth.LoginRequest
import com.englishlearningapp.data.remote.dto.auth.LoginResponse
import com.englishlearningapp.data.remote.dto.auth.UserDto
import com.englishlearningapp.data.remote.dto.auth.RegisterRequest
import com.englishlearningapp.core.network.BackendResult
import com.englishlearningapp.core.network.requireBackendData

/**
 * Repository that handles authentication operations.
 * Acts as a bridge between the API layer and the UI layer.
 */
class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    /**
     * Login with email and password.
     * On success, saves tokens to DataStore and returns the login data.
     */
    suspend fun login(email: String, password: String): LoginResponse {
        val response = authApi.login(LoginRequest(email, password))

        // Check if the API returned a successful status
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }

        // Save tokens for future authenticated requests
        val data = response.data
        tokenManager.saveTokens(data.accessToken, data.refreshToken)

        return data
    }

    suspend fun register(request: RegisterRequest): BackendResult<UserDto> =
        authApi.register(request).requireBackendData()

    /**
     * Get the currently authenticated user's profile.
     */
    suspend fun getCurrentUser(): UserDto {
        val response = authApi.me()

        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }

        return response.data
    }

    /**
     * Logout the current user.
     * Clears stored tokens regardless of API response.
     */
    suspend fun logout() {
        try {
            authApi.logout()
        } finally {
            // Always clear tokens, even if the API call fails
            tokenManager.clearTokens()
        }
    }
}
