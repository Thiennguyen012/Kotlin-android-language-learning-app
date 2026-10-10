package com.vu.englishlearningapp.data.repository

import com.vu.englishlearningapp.core.network.requireBackendData
import com.vu.englishlearningapp.data.remote.api.SpeakingAssessmentApi
import com.vu.englishlearningapp.data.remote.dto.speaking.SpeakingAssessmentDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

class SpeakingAssessmentRepository(private val api: SpeakingAssessmentApi) {
    suspend fun submit(file: File, sentence: String): SpeakingAssessmentDto {
        val audio = MultipartBody.Part.createFormData(
            "audio",
            if (file.name.endsWith(".m4a", ignoreCase = true)) file.name else "pronunciation.m4a",
            file.asRequestBody("audio/mp4".toMediaType())
        )
        return api.submit(
            sentence.toRequestBody("text/plain".toMediaType()),
            "en-us".toRequestBody("text/plain".toMediaType()),
            UUID.randomUUID().toString().toRequestBody("text/plain".toMediaType()),
            audio
        ).requireBackendData().data
    }

    suspend fun get(uuid: String): SpeakingAssessmentDto =
        api.get(uuid).requireBackendData().data
}
