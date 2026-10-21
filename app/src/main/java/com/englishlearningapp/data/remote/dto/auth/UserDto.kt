package com.englishlearningapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * User data returned from the backend.
 * Matches the "user" object inside the login response.
 */
data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String,
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("status") val status: Int,
    @SerializedName("is_super_admin") val isSuperAdmin: Boolean,
    @SerializedName("permissions") val permissions: List<PermissionDto> = emptyList(),
    @SerializedName("role_ids") val roleIds: List<Int> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("deleted_at") val deletedAt: String? = null
)
