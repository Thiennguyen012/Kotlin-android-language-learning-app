package com.englishlearningapp.data.repository

import com.englishlearningapp.core.network.requireBackendData
import com.englishlearningapp.data.remote.api.SpeakingLevelAssessmentApi
import com.englishlearningapp.data.remote.dto.speaking.CreateSpeakingLevelAssessmentRequest
import com.englishlearningapp.data.remote.dto.speaking.SpeakingLevelAssessmentDto
import com.englishlearningapp.data.remote.dto.speaking.SpeakingTopicDto
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class SpeakingLevelAssessmentRepository(private val api: SpeakingLevelAssessmentApi) {
    suspend fun topics(): List<SpeakingTopicDto> = api.topics().requireBackendData().data

    suspend fun create(topic: String): SpeakingLevelAssessmentDto =
        api.create(CreateSpeakingLevelAssessmentRequest(topic)).requireBackendData().data

    suspend fun submitAnswer(uuid: String, file: File): SpeakingLevelAssessmentDto {
        val audio = MultipartBody.Part.createFormData(
            "audio",
            file.name,
            file.asRequestBody("audio/mp4".toMediaType())
        )
        return api.submitAnswer(uuid, audio).requireBackendData().data
    }

    suspend fun get(uuid: String): SpeakingLevelAssessmentDto =
        api.get(uuid).requireBackendData().data
}
