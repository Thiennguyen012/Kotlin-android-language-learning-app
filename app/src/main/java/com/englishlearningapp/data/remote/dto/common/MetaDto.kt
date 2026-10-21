package com.englishlearningapp.data.remote.dto.common

import com.google.gson.annotations.SerializedName

/**
 * Pagination metadata returned by list endpoints.
 */
data class MetaDto(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int
)
