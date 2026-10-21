package com.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.englishlearningapp.data.remote.dto.quiz.AttemptHistoryDto
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.theme.AppScreenBackground

private val HistoryBackground = AppScreenBackground
private val HistoryAccent = Color(0xFF4968A8)
private val HistorySecondaryText = Color(0xFF697386)

@Composable
fun AttemptHistoryScreen(
    viewModel: AttemptHistoryViewModel,
    onAttemptClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = HistoryBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Test history",
                onBackClick = onBackClick,
                onRefreshClick = viewModel::loadHistory
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> HistoryMessage(innerPadding) {
                CircularProgressIndicator(color = HistoryAccent)
            }

            uiState.errorMessage != null && uiState.attempts.isEmpty() -> HistoryMessage(innerPadding) {
                Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = viewModel::loadHistory) { Text("Retry") }
            }

            uiState.attempts.isEmpty() -> HistoryMessage(innerPadding) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = HistorySecondaryText,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("You have not taken any tests yet", color = HistorySecondaryText)
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text("Your attempts", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${uiState.totalAttempts} attempts in total",
                            color = HistorySecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(uiState.attempts, key = { it.id }) { attempt ->
                    AttemptHistoryCard(
                        attempt = attempt,
                        onClick = { onAttemptClick(attempt.id) }
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = viewModel::previousPage,
                            enabled = uiState.currentPage > 1 && !uiState.isLoading
                        ) {
                            Text("Previous")
                        }

                        Text(
                            text = "Page ${uiState.currentPage} / ${uiState.lastPage}",
                            color = HistorySecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedButton(
                            onClick = viewModel::nextPage,
                            enabled = uiState.currentPage < uiState.lastPage && !uiState.isLoading
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttemptHistoryCard(
    attempt: AttemptHistoryDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(HistoryAccent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = HistoryAccent)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = attempt.collectionTest?.testName ?: "Unknown test",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (attempt.status == "submitted") {
                        HistoryValue(
                            icon = Icons.Default.CheckCircle,
                            text = "${attempt.result.correctCount}/${attempt.collectionTest?.totalQuestions ?: 0} correct"
                        )
                    }
                    HistoryValue(
                        icon = Icons.Default.Schedule,
                        text = attempt.totalTime ?: "--:--:--"
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatHistoryDate(attempt.submittedAt ?: attempt.startedAt),
                    color = HistorySecondaryText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = if (attempt.status == "submitted") "Done" else "In progress",
                color = if (attempt.status == "submitted") Color(0xFF2E7D32) else HistoryAccent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HistoryValue(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = HistorySecondaryText, modifier = Modifier.size(15.dp))
        Text(
            text = text,
            color = HistorySecondaryText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun HistoryMessage(innerPadding: PaddingValues, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
    }
}

private fun formatHistoryDate(value: String?): String {
    if (value.isNullOrBlank()) return "Not submitted"
    return value.replace('T', ' ').substringBefore('.').removeSuffix("Z").take(16)
}
