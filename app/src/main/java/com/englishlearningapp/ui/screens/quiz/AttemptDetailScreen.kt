package com.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import com.englishlearningapp.data.remote.dto.quiz.AttemptDetailDto
import com.englishlearningapp.ui.components.AppTopNavigationBar

private val AttemptDetailBackground = Color(0xFFFAF8F5)
private val AttemptDetailAccent = Color(0xFF4968A8)
private val AttemptDetailSecondary = Color(0xFF697386)

@Composable
fun AttemptDetailScreen(
    viewModel: AttemptDetailViewModel,
    canContinueAttempt: Boolean,
    onContinueTest: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AttemptDetailBackground,
        topBar = {
            AppTopNavigationBar(
                title = "Attempt details",
                onBackClick = onBackClick,
                onRefreshClick = viewModel::loadAttempt
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AttemptDetailAccent)
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = viewModel::loadAttempt) { Text("Retry") }
            }

            uiState.detail != null -> AttemptDetailContent(
                detail = uiState.detail!!,
                uiState = uiState,
                innerPadding = innerPadding,
                canContinueAttempt = canContinueAttempt,
                onContinueTest = onContinueTest,
                onQuestionClick = viewModel::loadQuestionDetail
            )
        }
    }
}

@Composable
private fun AttemptDetailContent(
    detail: AttemptDetailDto,
    uiState: AttemptDetailUiState,
    innerPadding: PaddingValues,
    canContinueAttempt: Boolean,
    onContinueTest: (Int) -> Unit,
    onQuestionClick: (Int) -> Unit
) {
    val attempt = detail.attempt
    val isSubmitted = attempt.status == "submitted"
    val totalQuestions = attempt.collectionTest?.totalQuestions ?: attempt.questions.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = AttemptDetailAccent,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = attempt.collectionTest?.testName ?: "Test attempt",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSubmitted) "Completed" else "In progress",
                        color = if (isSubmitted) Color(0xFF2E7D32) else AttemptDetailAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    if (isSubmitted) {
                        Text(
                            text = "${attempt.correctCount} / $totalQuestions",
                            color = AttemptDetailAccent,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Correct answers",
                            color = AttemptDetailSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Bạn đang làm bài dở bài test này",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { attempt.collectionTest?.id?.let(onContinueTest) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Làm bài tiếp", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    AttemptInfoRow("Started", formatAttemptDate(attempt.startedTime))
                    AttemptInfoRow("Submitted", formatAttemptDate(attempt.finishedTime))
                    AttemptInfoRow("Time used", attempt.totalTime ?: "In progress")
                    if (!isSubmitted && canContinueAttempt) {
                        AttemptInfoRow("Time remaining", formatRemainingTime(detail.remainingSeconds))
                    }
                }
            }
        }

        if (isSubmitted) {
            item {
                Text(
                    text = "Answers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (attempt.questions.isEmpty()) {
                item {
                    Text(
                        text = "No question information available",
                        color = AttemptDetailSecondary,
                        modifier = Modifier.padding(vertical = 18.dp)
                    )
                }
            } else {
                itemsIndexed(attempt.questions, key = { _, question -> question.id }) { index, question ->
                    val answer = question.answer
                    val isCorrect = answer?.isCorrect == true
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuestionClick(question.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSubmitted && answer != null) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                Text("Question ${index + 1}", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(question.questionText, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your answer: ${question.formatUserAnswer(answer?.userAnswer)}",
                                color = AttemptDetailSecondary
                            )
                            if (isSubmitted && !isCorrect) {
                                Text(
                                    text = "Correct answer: ${question.getCorrectAnswer()}",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            val isExpanded = uiState.expandedQuestionId == question.id
                            AnimatedVisibility(visible = isExpanded) {
                                QuestionInlinePreview(
                                    question = if (isExpanded) uiState.previewQuestion else null,
                                    isLoading = uiState.isQuestionLoading,
                                    error = uiState.questionError
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttemptInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AttemptDetailSecondary)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun formatAttemptDate(value: String?): String {
    if (value.isNullOrBlank()) return "--"
    return value.replace('T', ' ').substringBefore('.').removeSuffix("Z").take(16)
}

private fun formatRemainingTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
