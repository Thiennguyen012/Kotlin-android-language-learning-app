package com.englishlearningapp.data.remote.dto.quiz

import com.google.gson.annotations.SerializedName

/**
 * Test type info embedded in a collection test response.
 */
data class TestTypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("test_type") val testType: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
