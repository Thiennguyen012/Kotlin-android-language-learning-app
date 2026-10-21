package com.englishlearningapp.data.repository

import com.englishlearningapp.data.remote.api.AuthApi
import com.englishlearningapp.data.remote.api.ProfileApi
import com.englishlearningapp.data.remote.dto.auth.UserDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Repository for profile operations.
 * Reuses AuthApi.me() for fetching the profile.
 * Uses ProfileApi for updating profile fields.
 */
class ProfileRepository(
    private val authApi: AuthApi,
    private val profileApi: ProfileApi
) {

    /**
     * Get the current user's profile.
     * Reuses the existing /api/admin/auth/me endpoint.
     */
    suspend fun getProfile(): UserDto {
        val response = authApi.me()
        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    /**
     * Update the current user's profile.
     * Sends multipart/form-data with _method=PUT (Laravel method spoofing).
     *
     * @param name The user's display name.
     * @param phone The user's phone number.
     * @param birthday The user's birthday (format: YYYY-MM-DD or ISO string).
     * @param address The user's address.
     * @return Updated user data.
     */
    suspend fun updateProfile(
        name: String,
        phone: String,
        birthday: String,
        address: String,
        avatarBytes: ByteArray? = null,
        avatarFileName: String? = null,
        avatarMimeType: String? = null
    ): UserDto {
        // Helper to create text request bodies for multipart
        val textType = "text/plain".toMediaTypeOrNull()

        val avatarPart = avatarBytes?.let { bytes ->
            MultipartBody.Part.createFormData(
                name = "avatar",
                filename = avatarFileName ?: "avatar",
                body = bytes.toRequestBody(avatarMimeType?.toMediaTypeOrNull())
            )
        }

        val response = profileApi.updateProfile(
            method = "PUT".toRequestBody(textType),
            name = name.toRequestBody(textType),
            phone = phone.toRequestBody(textType),
            birthday = birthday.toRequestBody(textType),
            address = address.toRequestBody(textType),
            avatar = avatarPart
        )

        if (response.statusCode != 200 || response.data == null) {
            throw Exception(response.message)
        }

        return response.data
    }
}
