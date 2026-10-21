package com.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.quiz.QuestionDto
import com.englishlearningapp.ui.components.AppTopNavigationBar

@Composable
fun QuizTakingScreen(
    viewModel: QuizTakingViewModel,
    canAnswer: Boolean,
    canSubmit: Boolean,
    onQuizFinished: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAttemptStatus()
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) onQuizFinished()
    }

    LaunchedEffect(uiState.isAlreadySubmitted) {
        if (uiState.isAlreadySubmitted) onBackClick()
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = uiState.testName.ifEmpty { "Quiz" },
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> QuizStateContainer(innerPadding) {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null && uiState.questions.isEmpty() -> {
                QuizStateContainer(innerPadding) {
                    Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onBackClick) { Text("Back to quizzes") }
                }
            }

            uiState.questions.isEmpty() -> QuizStateContainer(innerPadding) {
                Text("No questions in this quiz", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBackClick) { Text("Back to quizzes") }
            }

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
            ) {
                QuizProgressHeader(
                    progress = uiState.progress,
                    remainingTime = uiState.formattedRemainingTime,
                    isTimeWarning = uiState.remainingSeconds <= 60,
                    progressFraction = (uiState.currentIndex + 1).toFloat() / uiState.questions.size
                )
                Spacer(Modifier.height(20.dp))
                Box(modifier = Modifier.weight(1f)) {
                    uiState.currentQuestion?.let { question ->
                        QuizQuestionCard(
                            question = question,
                            questionNumber = uiState.currentIndex + 1,
                            questionCount = uiState.questions.size,
                            currentIndex = uiState.currentIndex,
                            answer = uiState.currentSelectedAnswer,
                            actionError = uiState.answerErrorMessage ?: uiState.errorMessage,
                            isFirstQuestion = uiState.isFirstQuestion,
                            isLastQuestion = uiState.isLastQuestion,
                            isSavingAnswer = uiState.isSavingAnswer,
                            isSubmitting = uiState.isSubmitting,
                            remainingSeconds = uiState.remainingSeconds,
                            hasCurrentAnswer = uiState.hasCurrentAnswer,
                            canAnswer = canAnswer,
                            canSubmit = canSubmit,
                            onAnswerChanged = viewModel::updateAnswer,
                            onPrevious = viewModel::previousQuestion,
                            onNext = viewModel::saveAnswerAndContinue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizProgressHeader(
    progress: String,
    remainingTime: String,
    isTimeWarning: Boolean,
    progressFraction: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(progress, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            remainingTime,
            fontWeight = FontWeight.Bold,
            color = if (isTimeWarning) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { progressFraction },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun QuizQuestionCard(
    question: QuestionDto,
    questionNumber: Int,
    questionCount: Int,
    currentIndex: Int,
    answer: String?,
    actionError: String?,
    isFirstQuestion: Boolean,
    isLastQuestion: Boolean,
    isSavingAnswer: Boolean,
    isSubmitting: Boolean,
    remainingSeconds: Int,
    hasCurrentAnswer: Boolean,
    canAnswer: Boolean,
    canSubmit: Boolean,
    onAnswerChanged: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val inputEnabled = canAnswer && !isSavingAnswer && !isSubmitting
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primary) {
                Text(
                    text = "Question $questionNumber",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                )
            }
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp, bottom = 14.dp)
            )
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
            ) {
                QuizAnswerInput(
                    question = question,
                    answer = answer,
                    enabled = inputEnabled,
                    onAnswerChanged = onAnswerChanged
                )
            }
            actionError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = !isFirstQuestion && inputEnabled,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text("‹  Previous") }
                Button(
                    onClick = onNext,
                    enabled = hasCurrentAnswer && inputEnabled && remainingSeconds > 0 &&
                        (!isLastQuestion || canSubmit),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text(
                        when {
                            isSavingAnswer -> "Saving..."
                            isSubmitting -> "Submitting..."
                            isLastQuestion -> "Finish"
                            else -> "Next  ›"
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally)
            ) {
                repeat(questionCount) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentIndex) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizStateContainer(innerPadding: PaddingValues, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { content() }
}
