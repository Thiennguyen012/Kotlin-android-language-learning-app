package com.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.englishlearningapp.data.remote.dto.quiz.QuestionDto

@Composable
fun QuizAnswerInput(
    question: QuestionDto,
    answer: String?,
    enabled: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    when (question.typeKeyword) {
        "multiple_choice" -> MultipleChoiceAnswer(question, answer, enabled, onAnswerChanged)
        "true_false" -> TrueFalseAnswer(answer, enabled, onAnswerChanged)
        "fill_in_blank" -> FillBlankAnswer(answer, enabled, onAnswerChanged)
        "matching" -> MatchingAnswer(question, answer, enabled, onAnswerChanged)
        else -> Text(
            "This question type is not supported.",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun MultipleChoiceAnswer(
    question: QuestionDto,
    answer: String?,
    enabled: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    val options = question.getOptions()
    
    // Remember the shuffled options with their original indices to prevent reshuffling on recomposition
    val shuffledOptions = remember(question.id) {
        options.mapIndexed { index, option -> index to option }.shuffled()
    }
    
    val selectedIndex = answer?.toIntOrNull()?.takeIf { it in options.indices }
        ?: options.indexOf(answer).takeIf { it >= 0 }

    shuffledOptions.forEachIndexed { displayIndex, (originalIndex, option) ->
        MultipleChoiceOptionCard(
            label = ('A'.code + displayIndex).toChar().toString(),
            text = option,
            selected = selectedIndex == originalIndex,
            enabled = enabled,
            onClick = { onAnswerChanged(originalIndex.toString()) }
        )
    }
}

@Composable
fun MultipleChoiceQuestionCard(
    question: QuestionDto,
    questionNumber: Int,
    answer: String?,
    enabled: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
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
                modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
            )
            MultipleChoiceAnswer(question, answer, enabled, onAnswerChanged)
        }
    }
}

@Composable
private fun MultipleChoiceOptionCard(
    label: String,
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    .wrapContentSize(Alignment.Center)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TrueFalseAnswer(
    answer: String?,
    enabled: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    AnswerChoiceCard(
        text = "True",
        selected = answer.equals("true", ignoreCase = true) || answer == "1",
        enabled = enabled,
        onClick = { onAnswerChanged("true") }
    )
    AnswerChoiceCard(
        text = "False",
        selected = answer.equals("false", ignoreCase = true) || answer == "0",
        enabled = enabled,
        onClick = { onAnswerChanged("false") }
    )
}

@Composable
private fun FillBlankAnswer(
    answer: String?,
    enabled: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = answer.orEmpty(),
        onValueChange = onAnswerChanged,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Your Answer") },
        singleLine = true,
        supportingText = { Text("Answers are checked without case sensitivity.") }
    )
}

@Composable
private fun MatchingAnswer(
    question: QuestionDto,
    answer: String?,
    enabled: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    val pairs = question.getMatchingPairs()
    val selections = remember(question.id) {
        mutableStateMapOf<String, String>().apply { putAll(parseMatchingAnswer(answer)) }
    }
    var selectedLeft by remember(question.id) { mutableStateOf<String?>(null) }
    var selectedRight by remember(question.id) { mutableStateOf<String?>(null) }
    
    // Shuffle left and right options independently, remembered by question.id to avoid reshuffling on recomposition
    val leftOptions = remember(question.id) { pairs.map { it.left }.shuffled() }
    val rightOptions = remember(question.id) { pairs.map { it.right }.distinct().shuffled() }

    val pairColors = listOf(
        Color(0xFFE8F5EF) to Color(0xFF2E8B67), // Green
        Color(0xFFF1EAFB) to Color(0xFF7654A8), // Purple
        Color(0xFFFFF1E5) to Color(0xFFB56B2A), // Orange
        Color(0xFFE8F0FA) to Color(0xFF4968A8), // Blue
        Color(0xFFFFF0F3) to Color(0xFFC24075), // Pink
        Color(0xFFE0F7FA) to Color(0xFF007A87), // Teal
        Color(0xFFFFF8E1) to Color(0xFFA07800), // Amber/Gold
        Color(0xFFECEFF1) to Color(0xFF455A64), // Slate/Gray
        Color(0xFFEEF0FC) to Color(0xFF4358CD), // Indigo/Lavender
        Color(0xFFF1F8E9) to Color(0xFF558B2F)  // Lime/Olive
    )
    val publishAnswer = {
        onAnswerChanged(
            if (pairs.all { selections[it.left].isNullOrBlank().not() }) {
                buildMatchingAnswer(pairs.map { it.left }, selections)
            } else ""
        )
    }

    Column {
        Text(
            "Select one item from either column, then choose its match. Tap a matched item to remove the pair.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                leftOptions.forEach { left ->
                    val isSelected = selectedLeft == left
                    val isMatched = selections[left] != null
                    val originalIndex = pairs.indexOfFirst { it.left == left }
                    val colors = pairColors[originalIndex.coerceAtLeast(0) % pairColors.size]
                    MatchingChoice(
                        text = left,
                        selected = isSelected,
                        backgroundColor = if (isMatched) colors.first else Color.White,
                        borderColor = if (isMatched || isSelected) colors.second
                        else MaterialTheme.colorScheme.outlineVariant,
                        enabled = enabled,
                        onClick = {
                            val right = selectedRight
                            val matchedRight = selections[left]
                            when {
                                right != null -> {
                                    selections.entries.firstOrNull { it.value == right }?.key?.let { key ->
                                        selections.remove(key)
                                    }
                                    selections[left] = right
                                    selectedLeft = null
                                    selectedRight = null
                                    publishAnswer()
                                }
                                matchedRight != null && selectedLeft == null -> {
                                    selections.remove(left)
                                    publishAnswer()
                                }
                                else -> {
                                    selectedLeft = if (selectedLeft == left) null else left
                                    selectedRight = null
                                }
                            }
                        }
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                rightOptions.forEach { right ->
                    val matchedIndex = pairs.indexOfFirst { selections[it.left] == right }
                    val isMatched = matchedIndex >= 0
                    val isSelected = selectedRight == right
                    val colors = pairColors[(matchedIndex.takeIf { it >= 0 } ?: 0) % pairColors.size]
                    MatchingChoice(
                        text = right,
                        selected = isSelected,
                        backgroundColor = if (isMatched) colors.first else Color.White,
                        borderColor = when {
                            isMatched -> colors.second
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                        enabled = enabled,
                        onClick = {
                            val left = selectedLeft
                            val matchedLeft = selections.entries.firstOrNull { it.value == right }?.key
                            when {
                                left != null -> {
                                    matchedLeft?.let { selections.remove(it) }
                                    selections[left] = right
                                    selectedLeft = null
                                    selectedRight = null
                                    publishAnswer()
                                }
                                matchedLeft != null && selectedRight == null -> {
                                    selections.remove(matchedLeft)
                                    publishAnswer()
                                }
                                else -> {
                                    selectedRight = if (selectedRight == right) null else right
                                    selectedLeft = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchingChoice(
    text: String,
    selected: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AnswerChoiceCard(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick, enabled = enabled)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun parseMatchingAnswer(answer: String?): Map<String, String> {
    if (answer.isNullOrBlank()) return emptyMap()
    return runCatching {
        JsonParser.parseString(answer).asJsonArray.associate { element ->
            val pair = element.asJsonObject
            pair.get("left").asString to pair.get("right").asString
        }
    }.getOrDefault(emptyMap())
}

private fun buildMatchingAnswer(
    leftItems: List<String>,
    selections: Map<String, String>
): String = JsonArray().apply {
    leftItems.forEach { left ->
        add(JsonObject().apply {
            addProperty("left", left)
            addProperty("right", selections[left].orEmpty())
        })
    }
}.toString()
