package com.vu.englishlearningapp.ui.screens.speakinglevel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vu.englishlearningapp.core.network.toBackendMessage
import com.vu.englishlearningapp.data.remote.dto.speaking.SpeakingLevelAssessmentDto
import com.vu.englishlearningapp.data.remote.dto.speaking.SpeakingTopicDto
import com.vu.englishlearningapp.data.repository.SpeakingLevelAssessmentRepository
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpeakingLevelUiState(
    val topics: List<SpeakingTopicDto> = emptyList(),
    val selectedTopic: String? = null,
    val assessment: SpeakingLevelAssessmentDto? = null,
    val loading: Boolean = true,
    val preparationRemaining: Int = 0,
    val responseRemaining: Int = 0,
    val readyToRecord: Boolean = false,
    val recording: Boolean = false,
    val stopRequested: Boolean = false,
    val submitting: Boolean = false,
    val polling: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class SpeakingLevelViewModel(
    private val repository: SpeakingLevelAssessmentRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(SpeakingLevelUiState())
    val state: StateFlow<SpeakingLevelUiState> = mutableState.asStateFlow()
    private var timerJob: Job? = null
    private var pollingJob: Job? = null

    init {
        loadTopics()
    }

    fun selectTopic(topic: String) {
        mutableState.value = mutableState.value.copy(selectedTopic = topic, error = null)
    }

    fun createAssessment() {
        val topic = mutableState.value.selectedTopic ?: return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null, assessment = null)
            try {
                val assessment = repository.create(topic)
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    assessment = assessment,
                    preparationRemaining = assessment.preparationSeconds,
                    responseRemaining = assessment.responseSeconds,
                    readyToRecord = assessment.preparationSeconds == 0
                )
                startPreparationTimer()
            } catch (exception: Exception) {
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    error = exception.toBackendMessage()
                )
            }
        }
    }

    fun recordingStarted() {
        val assessment = mutableState.value.assessment ?: return
        timerJob?.cancel()
        mutableState.value = mutableState.value.copy(
            recording = true,
            readyToRecord = false,
            stopRequested = false,
            responseRemaining = assessment.responseSeconds,
            error = null
        )
        timerJob = viewModelScope.launch {
            while (mutableState.value.recording && mutableState.value.responseRemaining > 0) {
                delay(1_000)
                val remaining = mutableState.value.responseRemaining - 1
                mutableState.value = mutableState.value.copy(responseRemaining = remaining)
                if (remaining <= 0) {
                    mutableState.value = mutableState.value.copy(stopRequested = true)
                    return@launch
                }
            }
        }
    }

    fun requestStop() {
        if (mutableState.value.recording) {
            mutableState.value = mutableState.value.copy(stopRequested = true)
        }
    }

    fun submitRecordedAnswer(file: File?) {
        timerJob?.cancel()
        val assessment = mutableState.value.assessment
        mutableState.value = mutableState.value.copy(recording = false, stopRequested = false)
        if (assessment == null || file == null) {
            file?.delete()
            mutableState.value = mutableState.value.copy(
                readyToRecord = true,
                error = "The recording was too short. Please try again."
            )
            return
        }

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(submitting = true, error = null)
            try {
                val queued = repository.submitAnswer(assessment.id, file)
                mutableState.value = mutableState.value.copy(
                    submitting = false,
                    polling = true,
                    assessment = queued
                )
                watch(queued.id)
            } catch (exception: Exception) {
                mutableState.value = mutableState.value.copy(
                    submitting = false,
                    readyToRecord = true,
                    error = exception.toBackendMessage()
                )
            } finally {
                file.delete()
            }
        }
    }

    fun reset() {
        timerJob?.cancel()
        pollingJob?.cancel()
        mutableState.value = mutableState.value.copy(
            assessment = null,
            preparationRemaining = 0,
            responseRemaining = 0,
            readyToRecord = false,
            recording = false,
            stopRequested = false,
            submitting = false,
            polling = false,
            error = null,
            info = null
        )
    }

    private fun loadTopics() {
        viewModelScope.launch {
            try {
                val topics = repository.topics()
                mutableState.value = mutableState.value.copy(
                    topics = topics,
                    selectedTopic = topics.firstOrNull()?.id,
                    loading = false
                )
            } catch (exception: Exception) {
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    error = exception.toBackendMessage()
                )
            }
        }
    }

    private fun startPreparationTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (mutableState.value.preparationRemaining > 0) {
                delay(1_000)
                mutableState.value = mutableState.value.copy(
                    preparationRemaining = mutableState.value.preparationRemaining - 1
                )
            }
            mutableState.value = mutableState.value.copy(readyToRecord = true)
        }
    }

    private fun watch(uuid: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            repeat(40) {
                delay(15_000)
                try {
                    val assessment = repository.get(uuid)
                    val finished = assessment.status == "completed" || assessment.status == "failed"
                    mutableState.value = mutableState.value.copy(
                        assessment = assessment,
                        polling = !finished,
                        error = null
                    )
                    if (finished) return@launch
                } catch (exception: Exception) {
                    mutableState.value = mutableState.value.copy(
                        polling = false,
                        info = "Could not refresh the result: ${exception.toBackendMessage()}"
                    )
                    return@launch
                }
            }
            mutableState.value = mutableState.value.copy(
                polling = false,
                info = "Evaluation is taking longer than expected."
            )
        }
    }

    class Factory(
        private val repository: SpeakingLevelAssessmentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpeakingLevelViewModel::class.java)) {
                return SpeakingLevelViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
