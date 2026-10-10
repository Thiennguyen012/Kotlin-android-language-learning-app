package com.vu.englishlearningapp.ui.screens.speaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vu.englishlearningapp.core.network.toBackendMessage
import com.vu.englishlearningapp.data.remote.dto.speaking.SpeakingAssessmentDto
import com.vu.englishlearningapp.data.repository.SpeakingAssessmentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SpeakingPracticeUiState(
    val sentence: String = "Hello, how are you?",
    val recording: Boolean = false,
    val submitting: Boolean = false,
    val polling: Boolean = false,
    val assessment: SpeakingAssessmentDto? = null,
    val error: String? = null,
    val info: String? = null
)

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

    fun updateSentence(value: String) {
        mutableState.value = mutableState.value.copy(sentence = value.take(3000), error = null)
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
        val sentence = mutableState.value.sentence.trim()
        if (sentence.isBlank()) {
            file.delete()
            showError("Enter a sentence to practice.")
            return
        }
        if (file.length() > 10 * 1024 * 1024) {
            file.delete()
            showError("Audio must be smaller than 10 MB.")
            return
        }
        if (mutableState.value.submitting || mutableState.value.polling) return

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(
                submitting = true,
                assessment = null,
                error = null,
                info = null
            )
            try {
                val assessment = repository.submit(file, sentence)
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

    private fun watch(assessmentId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(polling = true, info = null)
            try {
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
