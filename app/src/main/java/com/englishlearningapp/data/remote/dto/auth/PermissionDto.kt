package com.englishlearningapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class PermissionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("permission_name") val permissionName: String,
    @SerializedName("descriptions") val description: String? = null
)
