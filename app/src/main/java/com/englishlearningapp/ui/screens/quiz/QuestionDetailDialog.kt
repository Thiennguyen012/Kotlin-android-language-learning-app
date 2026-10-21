package com.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.question.AdminQuestionDto

@Composable
fun QuestionInlinePreview(
    question: AdminQuestionDto?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else if (question != null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Question Type: ${question.questionType?.name ?: "Unknown"}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                QuestionDataPreview(question)
            }
        }
    }
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
            Text("Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEachIndexed { index, option ->
                    PreviewValue(
                        value = "${('A'.code + index).toChar()}. $option",
                        highlighted = option == correctText
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Correct answer: $correctText",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        "true_false" -> {
            val correctVal = data.get("correct")?.asBoolean ?: false
            Text("Correct answer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            PreviewValue(
                value = if (correctVal) "True" else "False",
                highlighted = true
            )
        }
        "fill_in_blank" -> {
            val answerVal = data.get("answer")?.asString.orEmpty()
            Text("Correct answer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            PreviewValue(
                value = answerVal,
                highlighted = true
            )
        }
        "matching" -> {
            Text("Correct matching pairs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.getAsJsonArray("pairs")?.forEach { element ->
                    val pair = element.asJsonObject
                    PreviewValue(
                        value = "${pair.get("left")?.asString.orEmpty()}   →   ${pair.get("right")?.asString.orEmpty()}"
                    )
                }
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
        Text(
            text = value,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
