package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.auth.PermissionDto
import com.englishlearningapp.data.remote.dto.auth.PermissionRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PermissionApi {
    @GET("api/admin/permissions")
    suspend fun getPermissions(): ApiResponse<List<PermissionDto>>

    @POST("api/admin/permissions")
    suspend fun createPermission(
        @Body request: PermissionRequestDto
    ): ApiResponse<PermissionDto>

    @PUT("api/admin/permissions/{id}")
    suspend fun updatePermission(
        @Path("id") id: Int,
        @Body request: PermissionRequestDto
    ): ApiResponse<PermissionDto>

    @DELETE("api/admin/permissions/{id}")
    suspend fun deletePermission(@Path("id") id: Int): ApiResponse<Unit>
}
