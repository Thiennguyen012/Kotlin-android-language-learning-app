package com.englishlearningapp.ui.screens.admin.flashcard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishlearningapp.ui.components.AppTopNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardFormScreen(
    viewModel: FlashcardFormViewModel,
    isEditMode: Boolean,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
            onSaveSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = if (isEditMode) "Edit Flashcard" else "Create Flashcard",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoadingFlashcard || uiState.isLoadingWordTypes && uiState.wordTypes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Original Word
                OutlinedTextField(
                    value = uiState.originalWord,
                    onValueChange = { viewModel.updateOriginalWord(it) },
                    label = { Text("Original Word") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.validationErrors.containsKey("originalWord"),
                    supportingText = {
                        uiState.validationErrors["originalWord"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Translated Word
                OutlinedTextField(
                    value = uiState.translatedWord,
                    onValueChange = { viewModel.updateTranslatedWord(it) },
                    label = { Text("Translated Word") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.validationErrors.containsKey("translatedWord"),
                    supportingText = {
                        uiState.validationErrors["translatedWord"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.explanation,
                    onValueChange = viewModel::updateExplanation,
                    label = { Text("Explanation (Optional)") },
                    placeholder = { Text("Add a short explanation or usage note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Word Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.selectedWordType?.displayValue ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Word Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = uiState.validationErrors.containsKey("wordType"),
                        supportingText = {
                            uiState.validationErrors["wordType"]?.let { error ->
                                Text(error, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        uiState.wordTypes.forEach { wordType ->
                            DropdownMenuItem(
                                text = { Text(wordType.displayValue) },
                                onClick = {
                                    viewModel.updateWordType(wordType.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveFlashcard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isEditMode) "Save Changes" else "Create Flashcard")
                    }
                }
            }
        }
    }
}
