package com.englishlearningapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * Request body for the login endpoint.
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
