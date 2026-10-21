package com.englishlearningapp.data.remote.api

import com.englishlearningapp.data.remote.dto.auth.ApiResponse
import com.englishlearningapp.data.remote.dto.role.RoleDto
import com.englishlearningapp.data.remote.dto.role.RoleRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RoleApi {
    @GET("api/admin/roles")
    suspend fun getRoles(): ApiResponse<List<RoleDto>>

    @POST("api/admin/roles")
    suspend fun createRole(@Body request: RoleRequestDto): ApiResponse<RoleDto>

    @PUT("api/admin/roles/{id}")
    suspend fun updateRole(
        @Path("id") id: Int,
        @Body request: RoleRequestDto
    ): ApiResponse<RoleDto>

    @DELETE("api/admin/roles/{id}")
    suspend fun deleteRole(@Path("id") id: Int): ApiResponse<Unit>
}
