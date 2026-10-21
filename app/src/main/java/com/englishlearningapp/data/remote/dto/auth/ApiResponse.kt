package com.englishlearningapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName
import com.englishlearningapp.data.remote.dto.common.MetaDto

/**
 * Wrapper for all API responses from the Laravel backend.
 * The backend always returns: status_code, message, data.
 * List endpoints also include a "meta" object with pagination info.
 */
data class ApiResponse<T>(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?,
    @SerializedName("meta") val meta: MetaDto? = null
)
