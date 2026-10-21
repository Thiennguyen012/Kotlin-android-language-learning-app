package com.englishlearningapp.ui.screens.speaking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.englishlearningapp.R
import com.englishlearningapp.ui.components.AppTopNavigationBar
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * UI phòng luyện phát âm.
 * Luồng thao tác: hiển thị câu -> xin quyền micro -> ghi âm -> dừng và nộp -> hiện kết quả.
 */
@Composable
fun SpeakingPracticeScreen(
    viewModel: SpeakingPracticeViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val recorder = remember(context) { AudioRecorder(context) }
    var inputLevel by remember { mutableFloatStateOf(0f) }
    var silentSince by remember { mutableLongStateOf(0L) }
    val startRecording: () -> Unit = {
        try {
            recorder.start()
            viewModel.setRecording(true)
        } catch (_: Exception) {
            viewModel.showError(context.getString(R.string.speaking_recording_error))
        }
    }
    // Android chỉ bắt đầu MediaRecorder sau khi người dùng cấp quyền RECORD_AUDIO.
    val permissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording()
        } else {
            viewModel.showError(context.getString(R.string.speaking_permission_denied))
        }
    }

    DisposableEffect(recorder) {
        onDispose { recorder.cancel() }
    }

    // Biên độ được đọc liên tục để waveform phản hồi theo âm thanh thật từ micro.
    LaunchedEffect(state.recording) {
        silentSince = System.currentTimeMillis()
        while (state.recording) {
            inputLevel = recorder.amplitude()
            if (inputLevel > 0.025f) silentSince = System.currentTimeMillis()
            delay(80)
        }
        inputLevel = 0f
    }

    Scaffold(
        topBar = {
            AppTopNavigationBar(
                title = stringResource(R.string.speaking_title),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.speaking_instructions),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = state.sentence,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.speaking_sentence_label)) },
                minLines = 2,
                maxLines = 4,
                readOnly = true,
                enabled = !state.recording && !state.submitting && !state.polling && !state.loadingSentence,
                trailingIcon = {
                    IconButton(
                        onClick = viewModel::newRandomSentence,
                        enabled = !state.recording && !state.submitting && !state.polling
                    ) {
                        if (state.loadingSentence) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.speaking_new_sentence)
                            )
                        }
                    }
                }
            )
            Text(
                text = stringResource(R.string.speaking_language_note, state.sentenceLevel),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = {
                    if (state.recording) {
                        // Dừng recorder trước, sau đó giao file M4A cho ViewModel upload.
                        viewModel.setRecording(false)
                        val audioFile = recorder.stop()
                        if (audioFile == null) {
                            viewModel.showError(context.getString(R.string.speaking_audio_too_short))
                        } else {
                            viewModel.submit(audioFile)
                        }
                    } else if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startRecording()
                    } else {
                        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loadingSentence && !state.submitting && !state.polling && state.sentence.isNotBlank()
            ) {
                Icon(
                    imageVector = if (state.recording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null
                )
                Text(
                    text = stringResource(
                        if (state.recording) R.string.speaking_stop_submit
                        else R.string.speaking_start_recording
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (state.recording) {
                AudioInputWave(level = inputLevel)
                Text(
                    text = when {
                        inputLevel > 0.025f -> "Microphone is receiving sound"
                        System.currentTimeMillis() - silentSince > 3_000L -> "No sound detected. Check microphone permission and emulator input."
                        else -> "Microphone is on. Speak into the microphone..."
                    },
                    color = when {
                        inputLevel > 0.025f -> MaterialTheme.colorScheme.primary
                        System.currentTimeMillis() - silentSince > 3_000L -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (state.submitting || state.polling) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(
                            if (state.submitting) R.string.speaking_uploading
                            else R.string.speaking_scoring
                        )
                    )
                }
            }
            state.error?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            state.info?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Assessment được cập nhật bởi polling trong ViewModel.
            state.assessment?.let { assessment ->
                when (assessment.status) {
                    "completed" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.speaking_result_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                assessment.result?.textScore?.score?.let { score ->
                                    Text(
                                        text = stringResource(R.string.speaking_score, score.roundToInt()),
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                                assessment.transcript?.let { transcript ->
                                    Text(stringResource(R.string.speaking_transcript, transcript))
                                }
                                val errors = assessment.result?.textScore?.differences?.errors.orEmpty()
                                if (errors.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.speaking_words_to_practice),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    errors.forEach { error ->
                                        Text(
                                            stringResource(
                                                R.string.speaking_word_error,
                                                error.word.orEmpty(),
                                                error.expected.orEmpty(),
                                                error.actual.orEmpty()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "failed" -> Text(
                        text = stringResource(R.string.speaking_failed, assessment.errorCode.orEmpty()),
                        color = MaterialTheme.colorScheme.error
                    )

                    else -> {
                        Text(stringResource(R.string.speaking_pending, assessment.status))
                        if (!state.polling) {
                            Button(onClick = viewModel::checkAgain) {
                                Text(stringResource(R.string.speaking_check_again))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AudioInputWave(level: Float) {
    val activeColor = MaterialTheme.colorScheme.primary
    val bars = remember { List(31) { it } }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(92.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bars.forEach { index ->
                val centerWeight = 1f - kotlin.math.abs(index - (bars.lastIndex / 2f)) / (bars.lastIndex / 2f)
                val pulse = kotlin.math.abs(
                    kotlin.math.sin(System.nanoTime() / 180_000_000.0 + index * 0.62)
                ).toFloat()
                val heightFraction = (0.10f + level.coerceAtLeast(0.025f) * pulse * (0.8f + centerWeight))
                    .coerceIn(0.10f, 1f)
                Canvas(modifier = Modifier.width(4.dp).height(64.dp)) {
                    val barHeight = size.height * heightFraction
                    drawRoundRect(
                        color = if (level > 0.025f) activeColor else activeColor.copy(alpha = 0.55f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, (size.height - barHeight) / 2f),
                        size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width, size.width)
                    )
                }
            }
        }
    }
}
