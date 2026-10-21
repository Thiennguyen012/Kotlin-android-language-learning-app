package com.englishlearningapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class PermissionRequestDto(
    @SerializedName("permission_name") val permissionName: String,
    @SerializedName("descriptions") val description: String
)
