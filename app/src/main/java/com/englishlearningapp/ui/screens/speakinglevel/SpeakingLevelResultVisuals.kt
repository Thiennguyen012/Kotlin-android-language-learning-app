package com.englishlearningapp.ui.screens.speakinglevel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.speaking.SpeakingLevelScoresDto
import kotlin.math.roundToInt

private data class ScoreItem(
    val shortLabel: String,
    val label: String,
    val score: Double,
    val color: Color
)

@Composable
fun OverallScoreSummary(score: Double, cefrLevel: String) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val animatedScore by animateFloatAsState(
        targetValue = (score / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "overall-score"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(118.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawArc(
                    color = track,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primary,
                    startAngle = -90f,
                    sweepAngle = animatedScore * 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = score.roundToInt().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("/ 100", style = MaterialTheme.typography.labelMedium)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Estimated speaking level", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = cefrLevel,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = primary
            )
            Text("Overall performance", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SpeakingCriteriaChart(scores: SpeakingLevelScoresDto) {
    val items = listOf(
        ScoreItem("PRO", "Pronunciation", scores.pronunciation, Color(0xFF4F6FB7)),
        ScoreItem("FLU", "Fluency", scores.fluency, Color(0xFF2A9D8F)),
        ScoreItem("GRA", "Grammar", scores.grammar, Color(0xFFF4A261)),
        ScoreItem("VOC", "Vocabulary", scores.vocabulary, Color(0xFF9B6BC6)),
        ScoreItem("COH", "Coherence", scores.coherence, Color(0xFFE76F51)),
        ScoreItem("TOP", "Topic", scores.topicRelevance, Color(0xFF3A86A8))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Score by criterion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEach { item ->
                    val animatedHeight by animateFloatAsState(
                        targetValue = (item.score / 100.0).toFloat().coerceIn(0f, 1f),
                        animationSpec = tween(850),
                        label = item.shortLabel
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.score.roundToInt().toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(0.62f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedHeight)
                                    .background(item.color)
                            )
                        }
                        Text(
                            text = item.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { item ->
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Box(Modifier.size(8.dp).background(item.color, CircleShape))
                                Text(item.label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CefrLevelScale(currentLevel: String) {
    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    val currentIndex = levels.indexOf(currentLevel.uppercase()).coerceAtLeast(0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CEFR level scale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Current: $currentLevel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                levels.forEachIndexed { index, level ->
                    val selected = index == currentIndex
                    val reached = index <= currentIndex
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            selected -> MaterialTheme.colorScheme.primary
                            reached -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = level,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Basic user", style = MaterialTheme.typography.labelSmall)
                Text("Independent user", style = MaterialTheme.typography.labelSmall)
                Text("Proficient user", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
