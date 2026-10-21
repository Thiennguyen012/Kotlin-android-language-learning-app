package com.vu.englishlearningapp.ui.screens.speakinglevel

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vu.englishlearningapp.R
import com.vu.englishlearningapp.data.remote.dto.speaking.SpeakingLevelResultDto
import com.vu.englishlearningapp.ui.components.AppTopNavigationBar
import com.vu.englishlearningapp.ui.screens.speaking.AudioRecorder
import kotlinx.coroutines.delay

@Composable
fun SpeakingLevelScreen(
    viewModel: SpeakingLevelViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val recorder = remember(context) { AudioRecorder(context) }
    var inputLevel by remember { mutableFloatStateOf(0f) }
    val recordingErrorMessage = stringResource(R.string.speaking_recording_error)
    val permissionDeniedMessage = stringResource(R.string.speaking_permission_denied)

    val startRecording = {
        try {
            recorder.start()
            viewModel.recordingStarted()
        } catch (_: Exception) {
            viewModel.recordingStartFailed(recordingErrorMessage)
        }
    }
    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startRecording() else viewModel.recordingStartFailed(permissionDeniedMessage)
    }
    val beginRecording = {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(state.autoStartRecording) {
        if (state.autoStartRecording) {
            viewModel.consumeAutoStartRecording()
            beginRecording()
        }
    }

    DisposableEffect(recorder) {
        onDispose { recorder.cancel() }
    }
    LaunchedEffect(state.recording) {
        while (state.recording) {
            inputLevel = recorder.amplitude()
            delay(80)
        }
        inputLevel = 0f
    }
    LaunchedEffect(state.stopRequested) {
        if (state.stopRequested) viewModel.submitRecordedAnswer(recorder.stop())
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                AppTopNavigationBar(title = "Speaking level assessment", onBackClick = onBackClick)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    state.loading -> CircularProgressIndicator()
                    state.assessment == null -> TopicSelection(state, viewModel)
                    else -> AssessmentContent(state, inputLevel, viewModel) {
                        if (state.recording) {
                            viewModel.requestStop()
                        } else {
                            beginRecording()
                        }
                    }
                }

                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                state.info?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }

        val assessment = state.assessment
        if (
            assessment != null &&
            assessment.status == "awaiting_answer" &&
            state.preparationRemaining > 0
        ) {
            PreparationCountdownOverlay(
                topic = assessment.topic,
                question = assessment.question,
                secondsRemaining = state.preparationRemaining,
                totalSeconds = assessment.preparationSeconds
            )
        }
    }
}

@Composable
private fun TopicSelection(state: SpeakingLevelUiState, viewModel: SpeakingLevelViewModel) {
    Text("Choose a topic", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("You will have time to prepare before recording your answer.")
    state.topics.chunked(2).forEach { rowTopics ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            rowTopics.forEach { topic ->
                FilterChip(
                    selected = state.selectedTopic == topic.id,
                    onClick = { viewModel.selectTopic(topic.id) },
                    label = { Text(topic.label) }
                )
            }
        }
    }
    Button(
        onClick = viewModel::createAssessment,
        enabled = state.selectedTopic != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Get question")
    }
}

@Composable
private fun AssessmentContent(
    state: SpeakingLevelUiState,
    inputLevel: Float,
    viewModel: SpeakingLevelViewModel,
    onRecordClick: () -> Unit
) {
    val assessment = state.assessment ?: return
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(assessment.topic.replace('-', ' ').replaceFirstChar { it.uppercase() })
            Text(assessment.question, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }

    if (assessment.status == "awaiting_answer") {
        when {
            state.preparationRemaining > 0 -> {
                Text("Preparation time", style = MaterialTheme.typography.labelLarge)
                Text("${state.preparationRemaining}s", style = MaterialTheme.typography.displaySmall)
                Text("Plan your main idea, reason and example.")
            }
            state.recording -> {
                Text("Time remaining: ${formatTime(state.responseRemaining)}", fontWeight = FontWeight.Bold)
                InputWave(inputLevel)
                Text(
                    if (inputLevel > 0.025f) "Microphone is receiving sound"
                    else "Speak clearly into the microphone",
                    color = if (inputLevel > 0.025f) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.readyToRecord -> Text("You are ready. Record one complete answer with reasons and examples.")
        }

        Button(
            onClick = onRecordClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.readyToRecord || state.recording
        ) {
            Icon(if (state.recording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
            Text(if (state.recording) "Stop and submit" else "Start answer", Modifier.padding(start = 8.dp))
        }
    }

    if (state.submitting || state.polling) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator()
            Text(if (state.submitting) "Uploading your answer..." else "Evaluating your speaking...")
        }
    }

    when (assessment.status) {
        "completed" -> assessment.result?.let { ResultCard(assessment.transcript.orEmpty(), it) }
        "failed" -> Text("Evaluation failed (${assessment.errorCode.orEmpty()}).", color = MaterialTheme.colorScheme.error)
    }

    if (assessment.status == "completed" || assessment.status == "failed") {
        Button(onClick = viewModel::reset, modifier = Modifier.fillMaxWidth()) {
            Text("Try another question")
        }
    }
}

@Composable
private fun ResultCard(transcript: String, result: SpeakingLevelResultDto) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OverallScoreSummary(result.overallScore, result.cefrLevel)
            }
        }
        CefrLevelScale(result.cefrLevel)
        SpeakingCriteriaChart(result.scores)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Your answer", fontWeight = FontWeight.Bold)
            Text(transcript)
            if (result.grammarErrors.isNotEmpty()) {
                Text("Grammar corrections", fontWeight = FontWeight.Bold)
                result.grammarErrors.forEach { error ->
                    Text("${error.original} \u2192 ${error.corrected}\n${error.explanation}")
                }
            }
            if (result.improvements.isNotEmpty()) {
                Text("How to improve", fontWeight = FontWeight.Bold)
                result.improvements.forEach { Text("\u2022 $it") }
            }
            if (result.upgradedAnswer.isNotBlank()) {
                Text("Upgraded answer", fontWeight = FontWeight.Bold)
                Text(result.upgradedAnswer)
            }
        }
    }
    }
}

@Composable
private fun InputWave(level: Float) {
    val color = MaterialTheme.colorScheme.primary
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            Modifier.fillMaxWidth().height(84.dp).padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(29) { index ->
                val scale = if (index % 3 == 0) 1f else 0.65f
                Canvas(Modifier.width(4.dp).height(56.dp)) {
                    val barHeight = size.height * (0.12f + level * scale).coerceAtMost(1f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(0f, (size.height - barHeight) / 2f),
                        size = Size(size.width, barHeight),
                        cornerRadius = CornerRadius(size.width, size.width)
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
