package com.englishlearningapp.data.repository

import com.englishlearningapp.core.network.BackendActionResult
import com.englishlearningapp.core.network.BackendResult
import com.englishlearningapp.core.network.requireBackendData
import com.englishlearningapp.core.network.requireBackendSuccess
import com.englishlearningapp.data.remote.api.PermissionApi
import com.englishlearningapp.data.remote.dto.auth.PermissionDto
import com.englishlearningapp.data.remote.dto.auth.PermissionRequestDto

class PermissionRepository(
    private val permissionApi: PermissionApi
) {
    suspend fun getPermissions(): List<PermissionDto> {
        val response = permissionApi.getPermissions()
        if (response.statusCode !in 200..299 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    suspend fun createPermission(
        request: PermissionRequestDto
    ): BackendResult<PermissionDto> = permissionApi.createPermission(request).requireBackendData()

    suspend fun updatePermission(
        id: Int,
        request: PermissionRequestDto
    ): BackendResult<PermissionDto> = permissionApi.updatePermission(id, request).requireBackendData()

    suspend fun deletePermission(id: Int): BackendActionResult =
        permissionApi.deletePermission(id).requireBackendSuccess()
}
