package com.englishlearningapp.data.remote.dto.role

import com.google.gson.annotations.SerializedName

data class RoleRequestDto(
    @SerializedName("role_name") val roleName: String,
    @SerializedName("descriptions") val description: String,
    @SerializedName("permission_ids") val permissionIds: List<Int>
)
