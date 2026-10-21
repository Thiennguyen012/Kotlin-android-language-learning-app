package com.englishlearningapp.ui.screens.admin.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.question.AdminQuestionDto

@Composable
fun QuestionPreviewDialog(
    question: AdminQuestionDto,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Question Details")
                Text(
                    question.questionType?.name ?: "Unknown type",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(question.questionText, fontWeight = FontWeight.SemiBold)
                QuestionDataPreview(question)
                if (!question.flashcardReferenceIds.isNullOrEmpty()) {
                    Text(
                        "Flashcard references: ${question.flashcardReferenceIds.orEmpty().joinToString()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onToggleSelection) {
                Text(if (isSelected) "Remove from test" else "Select question")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun QuestionDataPreview(question: AdminQuestionDto) {
    val data = question.getQuestionDataObject()
    when (question.questionType?.keyword) {
        "multiple_choice" -> {
            val options = data.getAsJsonArray("options")?.map { it.asString }.orEmpty()
            val correct = data.get("correct")
            val correctText = when {
                correct == null -> "--"
                correct.isJsonPrimitive && correct.asJsonPrimitive.isNumber ->
                    options.getOrNull(correct.asInt) ?: correct.asString
                else -> correct.asString
            }
            Text("Options", fontWeight = FontWeight.Medium)
            options.forEachIndexed { index, option ->
                PreviewValue(
                    value = "${index + 1}. $option",
                    highlighted = option == correctText
                )
            }
            Text("Correct answer: $correctText", color = MaterialTheme.colorScheme.primary)
        }
        "true_false" -> PreviewValue(
            value = "Correct answer: ${data.get("correct")?.asBoolean ?: false}",
            highlighted = true
        )
        "fill_in_blank" -> PreviewValue(
            value = "Correct answer: ${data.get("answer")?.asString.orEmpty()}",
            highlighted = true
        )
        "matching" -> {
            Text("Matching pairs", fontWeight = FontWeight.Medium)
            data.getAsJsonArray("pairs")?.forEach { element ->
                val pair = element.asJsonObject
                PreviewValue(
                    value = "${pair.get("left")?.asString.orEmpty()}  →  ${pair.get("right")?.asString.orEmpty()}"
                )
            }
        }
        else -> Text(data.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PreviewValue(value: String, highlighted: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(value, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
    }
}
