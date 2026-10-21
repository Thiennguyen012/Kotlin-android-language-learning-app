package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.auth.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit API interface for profile endpoints.
 * Update profile uses multipart/form-data with _method=PUT.
 */
interface ProfileApi {

    /**
     * Update the current user's profile.
     * Uses POST with _method=PUT (Laravel method spoofing).
     *
     * Required fields: _method, name, phone, birthday, address.
     * TODO: Add avatar upload via @Part avatar: MultipartBody.Part in the future.
     */
    @Multipart
    @POST("api/admin/auth/profile")
    suspend fun updateProfile(
        @Part("_method") method: RequestBody,
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("birthday") birthday: RequestBody,
        @Part("address") address: RequestBody,
        @Part avatar: MultipartBody.Part? = null
    ): ApiResponse<UserDto>
}
