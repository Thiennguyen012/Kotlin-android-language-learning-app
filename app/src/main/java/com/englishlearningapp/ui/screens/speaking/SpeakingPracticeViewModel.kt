package com.englishlearningapp.ui.screens.speaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.speaking.SpeakingAssessmentDto
import com.englishlearningapp.data.repository.SpeakingAssessmentRepository
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpeakingPracticeUiState(
    val sentenceId: Long? = null,
    val sentence: String = "",
    val sentenceLevel: String = "A2-B1",
    val loadingSentence: Boolean = true,
    val recording: Boolean = false,
    val submitting: Boolean = false,
    val polling: Boolean = false,
    val assessment: SpeakingAssessmentDto? = null,
    val error: String? = null,
    val info: String? = null
)

/**
 * Điều phối toàn bộ luồng luyện phát âm của màn hình:
 * lấy câu -> nhận file ghi âm -> tải file lên -> polling cho tới khi có kết quả.
 * Composable chỉ hiển thị [SpeakingPracticeUiState], còn network và trạng thái nằm ở đây.
 */
class SpeakingPracticeViewModel(
    private val repository: SpeakingAssessmentRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(SpeakingPracticeUiState())
    val state: StateFlow<SpeakingPracticeUiState> = mutableState.asStateFlow()
    private var pollingJob: Job? = null

    private companion object {
        const val STATUS_POLL_INTERVAL_MILLIS = 15_000L
        const val STATUS_POLL_ATTEMPTS = 48
    }

    init {
        // Mỗi lần mở phòng luyện phát âm, lấy một câu A2/B1 mới từ backend.
        loadRandomSentence()
    }

    fun newRandomSentence() {
        val currentState = mutableState.value
        if (
            currentState.recording ||
            currentState.submitting ||
            currentState.polling ||
            currentState.loadingSentence
        ) {
            return
        }

        loadRandomSentence(
            excludeId = currentState.sentenceId,
            previousText = currentState.sentence
        )
    }

    fun setRecording(value: Boolean) {
        mutableState.value = mutableState.value.copy(
            recording = value,
            error = null,
            assessment = if (value) null else mutableState.value.assessment
        )
    }

    fun showError(message: String) {
        mutableState.value = mutableState.value.copy(error = message)
    }

    fun submit(file: File) {
        val currentState = mutableState.value
        val sentence = currentState.sentence.trim()
        if (sentence.isBlank()) {
            file.delete()
            showError("No pronunciation sentence is available. Please try again.")
            return
        }
        if (file.length() > 10 * 1024 * 1024) {
            file.delete()
            showError("Audio must be smaller than 10 MB.")
            return
        }
        if (currentState.submitting || currentState.polling) return

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(
                submitting = true,
                assessment = null,
                error = null,
                info = null
            )
            try {
                // Backend trả về ngay trạng thái queued; việc chấm điểm tiếp tục trong Laravel queue.
                val assessment = repository.submit(file, currentState.sentenceId, sentence)
                mutableState.value = mutableState.value.copy(
                    submitting = false,
                    assessment = assessment
                )
                if (assessment.status != "completed" && assessment.status != "failed") {
                    watch(assessment.id)
                }
            } catch (exception: Exception) {
                mutableState.value = mutableState.value.copy(
                    submitting = false,
                    error = exception.toBackendMessage()
                )
            } finally {
                file.delete()
            }
        }
    }

    fun checkAgain() {
        val assessmentId = mutableState.value.assessment?.id ?: return
        if (pollingJob?.isActive != true) watch(assessmentId)
    }

    private fun loadRandomSentence(
        excludeId: Long? = null,
        previousText: String? = null
    ) {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(
                loadingSentence = true,
                assessment = null,
                error = null,
                info = null
            )
            try {
                var sentence = repository.randomSentence(excludeId)
                if (sentence.text == previousText) {
                    sentence = repository.randomSentence(sentence.id)
                }
                mutableState.value = mutableState.value.copy(
                    sentenceId = sentence.id,
                    sentence = sentence.text,
                    sentenceLevel = sentence.level,
                    loadingSentence = false
                )
            } catch (_: Exception) {
                // Vẫn cho phép luyện khi API lấy câu tạm thời không truy cập được.
                mutableState.value = mutableState.value.copy(
                    sentenceId = null,
                    sentence = PronunciationSentenceBank.random(excluding = previousText),
                    sentenceLevel = "A2-B1",
                    loadingSentence = false,
                    info = "Using an offline practice sentence."
                )
            }
        }
    }

    private fun watch(assessmentId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(polling = true, info = null)
            try {
                // Queue có thể mất vài phút khi model vừa khởi động, vì vậy không giữ HTTP request mở.
                repeat(STATUS_POLL_ATTEMPTS) {
                    delay(STATUS_POLL_INTERVAL_MILLIS)
                    val assessment = repository.get(assessmentId)
                    mutableState.value = mutableState.value.copy(assessment = assessment)
                    if (assessment.status == "completed" || assessment.status == "failed") {
                        mutableState.value = mutableState.value.copy(polling = false)
                        return@launch
                    }
                }
                mutableState.value = mutableState.value.copy(
                    polling = false,
                    info = "Scoring is taking longer than usual. Check again in a moment."
                )
            } catch (exception: Exception) {
                mutableState.value = mutableState.value.copy(
                    polling = false,
                    info = "Could not refresh the assessment status: ${exception.toBackendMessage()}"
                )
            }
        }
    }

    class Factory(
        private val repository: SpeakingAssessmentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpeakingPracticeViewModel::class.java)) {
                return SpeakingPracticeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
