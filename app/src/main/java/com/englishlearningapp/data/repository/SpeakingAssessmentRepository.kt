package com.englishlearningapp.data.repository

import com.englishlearningapp.core.network.requireBackendData
import com.englishlearningapp.data.remote.api.SpeakingAssessmentApi
import com.englishlearningapp.data.remote.dto.speaking.PronunciationSentenceDto
import com.englishlearningapp.data.remote.dto.speaking.SpeakingAssessmentDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

/**
 * Ranh giới dữ liệu của phòng luyện phát âm.
 * Repository chuyển file ghi âm thành multipart và chỉ trả DTO đã được backend xác nhận.
 */
class SpeakingAssessmentRepository(private val api: SpeakingAssessmentApi) {
    suspend fun randomSentence(excludeId: Long? = null): PronunciationSentenceDto =
        api.randomSentence(excludeId).requireBackendData().data

    suspend fun submit(
        file: File,
        sentenceId: Long?,
        sentence: String
    ): SpeakingAssessmentDto {
        // MediaRecorder tạo AAC trong container M4A, tương ứng content type audio/mp4.
        val audio = MultipartBody.Part.createFormData(
            "audio",
            if (file.name.endsWith(".m4a", ignoreCase = true)) file.name else "pronunciation.m4a",
            file.asRequestBody("audio/mp4".toMediaType())
        )
        return api.submit(
            sentenceId?.toString()?.toRequestBody("text/plain".toMediaType()),
            sentence.toRequestBody("text/plain".toMediaType()),
            "en-us".toRequestBody("text/plain".toMediaType()),
            // Mỗi lần thu âm dùng một khóa mới để backend chống tạo trùng khi request được gửi lại.
            UUID.randomUUID().toString().toRequestBody("text/plain".toMediaType()),
            audio
        ).requireBackendData().data
    }

    suspend fun get(uuid: String): SpeakingAssessmentDto =
        api.get(uuid).requireBackendData().data
}
