package com.englishlearningapp.ui.screens.admin.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardDto

@Composable
internal fun CollectionFlashcardPickerDialog(
    flashcards: List<FlashcardDto>,
    selectedIds: Set<Int>,
    isLoading: Boolean,
    isSaving: Boolean,
    onToggle: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val visibleFlashcards = remember(flashcards, searchQuery) {
        flashcards.filter { flashcard ->
            searchQuery.isBlank() ||
                flashcard.originalWord.contains(searchQuery, ignoreCase = true) ||
                flashcard.translatedWord.contains(searchQuery, ignoreCase = true) ||
                flashcard.explanation?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attach flashcards") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search flashcards") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                when {
                    isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    flashcards.isEmpty() -> Text("All flashcards are already attached.")
                    visibleFlashcards.isEmpty() -> Text("No matching flashcards.")
                    else -> LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                        items(visibleFlashcards, key = { it.id }) { flashcard ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isSaving) { onToggle(flashcard.id) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = flashcard.id in selectedIds,
                                    onCheckedChange = { onToggle(flashcard.id) },
                                    enabled = !isSaving
                                )
                                Column {
                                    Text(flashcard.originalWord)
                                    Text(flashcard.translatedWord)
                                    flashcard.explanation?.takeIf { it.isNotBlank() }?.let {
                                        Text(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedIds.isNotEmpty() && !isLoading && !isSaving
            ) {
                if (isSaving) CircularProgressIndicator() else Text("Attach (${selectedIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
        }
    )
}
