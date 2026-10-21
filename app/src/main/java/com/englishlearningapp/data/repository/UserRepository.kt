package com.englishlearningapp.data.repository

import com.englishlearningapp.core.network.BackendActionResult
import com.englishlearningapp.core.network.BackendResult
import com.englishlearningapp.core.network.requireBackendData
import com.englishlearningapp.core.network.requireBackendSuccess
import com.englishlearningapp.data.remote.api.UserApi
import com.englishlearningapp.data.remote.dto.auth.UserDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class UserFormPayload(
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val birthday: String,
    val address: String,
    val status: Int,
    val isSuperAdmin: Boolean,
    val roleIds: List<Int>,
    val avatarBytes: ByteArray? = null,
    val avatarFileName: String? = null,
    val avatarMimeType: String? = null
)

class UserRepository(private val userApi: UserApi) {
    suspend fun getUsers(): List<UserDto> {
        val response = userApi.getUsers()
        if (response.statusCode !in 200..299 || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }

    suspend fun getUser(id: Int): UserDto = userApi.getUser(id).requireBackendData().data

    suspend fun createUser(payload: UserFormPayload): BackendResult<UserDto> =
        userApi.createUser(payload.toMultipartParts(isUpdate = false)).requireBackendData()

    suspend fun updateUser(id: Int, payload: UserFormPayload): BackendResult<UserDto> =
        userApi.updateUser(id, payload.toMultipartParts(isUpdate = true)).requireBackendData()

    suspend fun deleteUser(id: Int): BackendActionResult =
        userApi.deleteUser(id).requireBackendSuccess()

    private fun UserFormPayload.toMultipartParts(isUpdate: Boolean): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()
        if (isUpdate) parts += textPart("_method", "PUT")
        parts += textPart("name", name)
        parts += textPart("email", email)
        if (password.isNotBlank()) parts += textPart("password", password)
        if (phone.isNotBlank()) parts += textPart("phone", phone)
        if (birthday.isNotBlank()) parts += textPart("birthday", birthday)
        if (address.isNotBlank()) parts += textPart("address", address)
        parts += textPart("status", status.toString())
        parts += textPart("is_super_admin", if (isSuperAdmin) "1" else "0")
        roleIds.forEach { roleId -> parts += textPart("role_ids[]", roleId.toString()) }
        avatarBytes?.let { bytes ->
            val body = bytes.toRequestBody(
                avatarMimeType?.toMediaTypeOrNull() ?: "image/*".toMediaType()
            )
            parts += MultipartBody.Part.createFormData(
                "avatar",
                avatarFileName ?: "avatar",
                body
            )
        }
        return parts
    }

    private fun textPart(name: String, value: String): MultipartBody.Part =
        MultipartBody.Part.createFormData(name, value)
}
