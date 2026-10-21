package com.englishlearningapp.data.remote.dto.role

import com.google.gson.annotations.SerializedName

data class RoleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("role_name") val roleName: String,
    @SerializedName("descriptions") val description: String? = null,
    @SerializedName("permission_ids") val permissionIds: List<Int> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
