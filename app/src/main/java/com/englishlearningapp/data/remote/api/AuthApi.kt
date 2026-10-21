package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.auth.LoginRequest
import com.englishlearningapp.data.remote.dto.auth.LoginResponse
import com.englishlearningapp.data.remote.dto.auth.RefreshTokenRequest
import com.englishlearningapp.data.remote.dto.auth.RegisterRequest
import com.englishlearningapp.data.remote.dto.auth.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API interface for authentication endpoints.
 * Base path: /api/admin/auth/
 */
interface AuthApi {

    @POST("api/admin/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("api/admin/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserDto>

    @GET("api/admin/auth/me")
    suspend fun me(): ApiResponse<UserDto>

    @POST("api/admin/auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @POST("api/admin/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<LoginResponse>
}
