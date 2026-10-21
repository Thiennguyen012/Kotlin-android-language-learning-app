package com.englishlearningapp.ui.screens.admin.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuestionDataFields(
    state: QuestionFormUiState,
    viewModel: QuestionFormViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (state.selectedType?.keyword) {
            "multiple_choice" -> MultipleChoiceFields(state, viewModel)
            "true_false" -> TrueFalseFields(state, viewModel)
            "fill_in_blank" -> FillBlankFields(state, viewModel)
            "matching" -> MatchingFields(state, viewModel)
            null -> Text(
                "Select a question type to configure its answer format.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> Text("Unsupported question type", color = MaterialTheme.colorScheme.error)
        }
        state.validationErrors["questionData"]?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MultipleChoiceFields(
    state: QuestionFormUiState,
    viewModel: QuestionFormViewModel
) {
    Text("Answer Options", style = MaterialTheme.typography.titleMedium)
    state.options.forEachIndexed { index, option ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = state.correctOptionIndex == index,
                onClick = { viewModel.selectCorrectOption(index) }
            )
            OutlinedTextField(
                value = option,
                onValueChange = { viewModel.updateOption(index, it) },
                label = { Text("Option ${index + 1}") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { viewModel.removeOption(index) },
                enabled = state.options.size > 2
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove option")
            }
        }
    }
    TextButton(onClick = viewModel::addOption) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Add option")
    }
    Text(
        "Select the radio button beside the correct option.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun TrueFalseFields(
    state: QuestionFormUiState,
    viewModel: QuestionFormViewModel
) {
    Text("Correct Answer", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = state.trueFalseAnswer,
                onClick = { viewModel.updateTrueFalseAnswer(true) }
            )
            Text("True")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !state.trueFalseAnswer,
                onClick = { viewModel.updateTrueFalseAnswer(false) }
            )
            Text("False")
        }
    }
}

@Composable
private fun FillBlankFields(
    state: QuestionFormUiState,
    viewModel: QuestionFormViewModel
) {
    OutlinedTextField(
        value = state.fillBlankAnswer,
        onValueChange = viewModel::updateFillBlankAnswer,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Correct Answer") },
        singleLine = true
    )
}

@Composable
private fun MatchingFields(
    state: QuestionFormUiState,
    viewModel: QuestionFormViewModel
) {
    Text("Matching Pairs", style = MaterialTheme.typography.titleMedium)
    state.matchingPairs.forEachIndexed { index, pair ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pair ${index + 1}", modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { viewModel.removeMatchingPair(index) },
                    enabled = state.matchingPairs.size > 1
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove pair")
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = pair.left,
                    onValueChange = { viewModel.updateMatchingLeft(index, it) },
                    label = { Text("Left") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = pair.right,
                    onValueChange = { viewModel.updateMatchingRight(index, it) },
                    label = { Text("Right") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    TextButton(onClick = viewModel::addMatchingPair) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Add pair")
    }
}
