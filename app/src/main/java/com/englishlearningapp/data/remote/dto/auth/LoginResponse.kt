package com.englishlearningapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * The "data" field returned from the login endpoint.
 * Contains the user object and token information.
 */
data class LoginResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("access_token_expires_in") val accessTokenExpiresIn: Int,
    @SerializedName("refresh_token_expires_in") val refreshTokenExpiresIn: Int
)
