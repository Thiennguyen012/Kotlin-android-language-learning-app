package com.englishlearningapp.ui.screens.admin.question

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.englishlearningapp.ui.components.AppTopNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormScreen(
    viewModel: QuestionFormViewModel,
    isEditMode: Boolean,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
            onSaveSuccess()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = if (isEditMode) "Edit Question" else "Create Question",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ExposedDropdownMenuBox(
                        expanded = typeMenuExpanded,
                        onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedType?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Question Type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                            },
                            isError = uiState.validationErrors.containsKey("questionType"),
                            supportingText = {
                                uiState.validationErrors["questionType"]?.let { Text(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            uiState.questionTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        androidx.compose.foundation.layout.Column {
                                            Text(type.name)
                                            type.description?.takeIf { it.isNotBlank() }?.let {
                                                Text(it, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectType(type.id)
                                        typeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = uiState.questionText,
                        onValueChange = viewModel::updateQuestionText,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Question Text") },
                        minLines = 2,
                        maxLines = 4,
                        isError = uiState.validationErrors.containsKey("questionText"),
                        supportingText = {
                            Text(
                                uiState.validationErrors["questionText"]
                                    ?: "${uiState.questionText.length}/255"
                            )
                        }
                    )
                }

                item {
                    QuestionDataFields(state = uiState, viewModel = viewModel)
                }

                item {
                    Button(
                        onClick = viewModel::saveQuestion,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(strokeWidth = 2.dp)
                        else Text(if (isEditMode) "Save Changes" else "Create Question")
                    }
                }
            }
        }
    }
}
