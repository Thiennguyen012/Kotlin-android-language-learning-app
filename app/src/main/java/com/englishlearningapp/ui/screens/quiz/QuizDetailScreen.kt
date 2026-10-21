package com.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.theme.AppScreenBackground

private val DetailBackground = AppScreenBackground
private val DetailAccent = Color(0xFF4968A8)
private val DetailSecondaryText = Color(0xFF697386)

@Composable
fun QuizDetailScreen(
    viewModel: QuizDetailViewModel,
    canStartAttempt: Boolean,
    canViewAttempt: Boolean,
    onStartTest: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DetailBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Test details",
                onBackClick = onBackClick,
                onRefreshClick = viewModel::loadTestDetail
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
                CircularProgressIndicator(color = DetailAccent)
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = viewModel::loadTestDetail) {
                    Text("Retry")
                }
            }

            uiState.test != null -> {
                val test = uiState.test ?: return@Scaffold
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DetailAccent.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = DetailAccent,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = test.testName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = test.description?.takeIf { it.isNotBlank() }
                                    ?: "Review the information before starting your attempt.",
                                color = DetailSecondaryText,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(28.dp))
                            DetailInformationRow(
                                icon = Icons.Default.Quiz,
                                label = "Questions",
                                value = "${test.totalQuestions} questions"
                            )
                            DetailInformationRow(
                                icon = Icons.Default.Timer,
                                label = "Time limit",
                                value = "${test.duration} minutes"
                            )
                            DetailInformationRow(
                                icon = Icons.Default.CalendarMonth,
                                label = "Open from",
                                value = formatTestDate(test.startedAt)
                            )
                            DetailInformationRow(
                                icon = Icons.Default.CalendarMonth,
                                label = "Open until",
                                value = formatTestDate(test.finishedAt)
                            )
                            DetailInformationRow(
                                icon = Icons.Default.Info,
                                label = "Status",
                                value = uiState.availability.displayName,
                                valueColor = uiState.availability.displayColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    if (uiState.availability == TestAvailability.EXPIRED) {
                        Text(
                            text = "This test has expired and can no longer be started.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                        )
                    } else {
                        if (uiState.activeAttemptId != null) {
                            Text(
                                text = "Bạn đang làm dở bài test này.",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                            )
                        } else {
                            Text(
                                text = "The countdown starts immediately after you press Start Test.",
                                color = DetailSecondaryText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                            )
                        }
                        val canOpenAttempt = canStartAttempt && canViewAttempt
                        if (canOpenAttempt) Button(
                            onClick = { onStartTest(test.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DetailAccent)
                        ) {
                            Text(
                                text = if (uiState.activeAttemptId != null) "Làm tiếp" else "Start Test",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInformationRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(DetailAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DetailAccent, modifier = Modifier.size(21.dp))
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(text = label, color = DetailSecondaryText, style = MaterialTheme.typography.bodySmall)
            Text(
                text = value,
                color = valueColor,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun formatTestDate(value: String?): String {
    if (value.isNullOrBlank()) return "No limit"
    return value
        .replace('T', ' ')
        .substringBefore('.')
        .removeSuffix("Z")
        .take(16)
}

private val TestAvailability.displayName: String
    get() = when (this) {
        TestAvailability.UPCOMING -> "Not started"
        TestAvailability.OPEN -> "Open"
        TestAvailability.EXPIRED -> "Expired"
        TestAvailability.AVAILABLE -> "Available"
    }

private val TestAvailability.displayColor: Color
    get() = when (this) {
        TestAvailability.UPCOMING -> Color(0xFFE68A00)
        TestAvailability.OPEN,
        TestAvailability.AVAILABLE -> Color(0xFF2E7D32)
        TestAvailability.EXPIRED -> Color(0xFFC62828)
    }
