package com.englishlearningapp.data.remote.dto.flashcard

import com.google.gson.annotations.SerializedName

/**
 * Represents a Word Type or Test Type used in dropdowns.
 */
data class WordTypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("test_type") val testType: String? = null,
    @SerializedName("type_name") val typeName: String? = null
) {
    val displayValue: String
        get() = typeName ?: testType ?: "Unknown"
}
