package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.auth.UserDto
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {
    @GET("api/admin/users")
    suspend fun getUsers(): ApiResponse<List<UserDto>>

    @GET("api/admin/users/{id}")
    suspend fun getUser(@Path("id") id: Int): ApiResponse<UserDto>

    @Multipart
    @POST("api/admin/users")
    suspend fun createUser(@Part parts: List<MultipartBody.Part>): ApiResponse<UserDto>

    @Multipart
    @POST("api/admin/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Part parts: List<MultipartBody.Part>
    ): ApiResponse<UserDto>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): ApiResponse<Unit>
}
