package com.englishlearningapp.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.theme.AppScreenBackground

private val ResultAccent = Color(0xFF4968A8)
private val ResultSecondary = Color(0xFF697386)

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBackToQuizzes: () -> Unit
) {
    val result = QuizResultHolder.result
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppScreenBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Quiz Result",
                onBackClick = onBackToQuizzes
            )
        }
    ) { innerPadding ->
        if (result == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No result data available")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackToQuizzes) {
                        Text("Back to Quizzes")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --- Score Card ---
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
                                tint = ResultAccent,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = result.testName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Completed",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "${result.score} / ${result.total}",
                                color = ResultAccent,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val percentage = if (result.total > 0) {
                                (result.score * 100) / result.total
                            } else 0
                            
                            Text(
                                text = "$percentage% correct answers",
                                color = ResultSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // --- Details Card ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            ResultInfoRow("Started", formatAttemptDate(result.startedTime))
                            ResultInfoRow("Submitted", formatAttemptDate(result.finishedTime))
                            ResultInfoRow("Time used", result.totalTime ?: "N/A")
                        }
                    }
                }

                // --- Review Header ---
                item {
                    Text(
                        text = "Answers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // --- Review Items ---
                itemsIndexed(result.reviewItems) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.loadQuestionDetail(item.questionId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (item.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (item.isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Question ${index + 1}", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.questionText, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your answer: ${item.userAnswer}",
                                color = ResultSecondary
                            )
                            if (!item.isCorrect) {
                                Text(
                                    text = "Correct answer: ${item.correctAnswer}",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            val isExpanded = uiState.expandedQuestionId == item.questionId
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

                // --- Back Button ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onBackToQuizzes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Back to Quizzes",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ResultSecondary)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun formatAttemptDate(value: String?): String {
    if (value.isNullOrBlank()) return "--"
    return value.replace('T', ' ').substringBefore('.').removeSuffix("Z").take(16)
}
