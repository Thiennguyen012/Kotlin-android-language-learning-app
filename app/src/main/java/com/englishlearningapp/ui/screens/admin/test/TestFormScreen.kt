package com.englishlearningapp.ui.screens.admin.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.components.AppDatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestFormScreen(
    viewModel: TestFormViewModel,
    isEditMode: Boolean,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var testTypeExpanded by remember { mutableStateOf(false) }
    var collectionExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var showQuestionPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaveSuccess) {
        if (state.isSaveSuccess) {
            state.successMessage?.let { snackbar.showSnackbar(it) }
            onSaveSuccess()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = if (isEditMode) "Edit Test" else "Create Test",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
            ) {
                item {
                    SelectionDropdown(
                        label = "Test Type",
                        value = state.selectedTestType?.testType.orEmpty(),
                        expanded = testTypeExpanded,
                        error = state.validationErrors["testType"],
                        onExpandedChange = { testTypeExpanded = it }
                    ) {
                        state.testTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.testType) },
                                onClick = {
                                    viewModel.selectTestType(type.id)
                                    testTypeExpanded = false
                                }
                            )
                        }
                    }
                }
                item {
                    SelectionDropdown(
                        label = "Flashcard Collection",
                        value = state.selectedCollection?.collectionName.orEmpty(),
                        expanded = collectionExpanded,
                        error = state.validationErrors["collection"],
                        onExpandedChange = { collectionExpanded = it }
                    ) {
                        state.collections.forEach { collection ->
                            DropdownMenuItem(
                                text = { Text(collection.collectionName) },
                                onClick = {
                                    viewModel.selectCollection(collection.id)
                                    collectionExpanded = false
                                }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.testName,
                        onValueChange = viewModel::updateName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Test Name") },
                        isError = state.validationErrors.containsKey("name"),
                        supportingText = { state.validationErrors["name"]?.let { Text(it) } },
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = viewModel::updateDescription,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 4,
                        isError = state.validationErrors.containsKey("description"),
                        supportingText = { Text(state.validationErrors["description"] ?: "${state.description.length}/2000") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.duration,
                        onValueChange = viewModel::updateDuration,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Duration (minutes)") },
                        isError = state.validationErrors.containsKey("duration"),
                        supportingText = { state.validationErrors["duration"]?.let { Text(it) } },
                        singleLine = true
                    )
                }
                item {
                    SelectionDropdown(
                        label = "Status",
                        value = if (state.status == 1) "Active" else "Inactive",
                        expanded = statusExpanded,
                        error = null,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        DropdownMenuItem(text = { Text("Active") }, onClick = {
                            viewModel.updateStatus(1); statusExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Inactive") }, onClick = {
                            viewModel.updateStatus(0); statusExpanded = false
                        })
                    }
                }
                item {
                    DateTimeField(
                        label = "Open From",
                        value = state.startedAt,
                        error = state.validationErrors["startedAt"],
                        onValueChange = viewModel::updateStartedAt
                    )
                }
                item {
                    DateTimeField(
                        label = "Open Until",
                        value = state.finishedAt,
                        error = state.validationErrors["finishedAt"],
                        onValueChange = viewModel::updateFinishedAt
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Questions", fontWeight = FontWeight.Bold)
                            Text(
                                "${state.selectedQuestionIds.size} of ${state.questions.size} selected",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            OutlinedButton(
                                onClick = { showQuestionPicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (state.selectedQuestionIds.isEmpty()) "Select questions" else "Review selection")
                            }
                        }
                        state.validationErrors["questions"]?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = viewModel::saveTest,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (state.isSaving) CircularProgressIndicator(strokeWidth = 2.dp)
                        else Text(if (isEditMode) "Save Changes" else "Create Test")
                    }
                }
            }
        }
    }

    if (showQuestionPicker) {
        TestQuestionPicker(
            state = state,
            onSearchChange = viewModel::updateQuestionSearch,
            onTypeFilterChange = viewModel::updateQuestionTypeFilter,
            onShowSelectedChange = viewModel::updateShowSelectedOnly,
            onToggleQuestion = viewModel::toggleQuestion,
            onDismiss = { showQuestionPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    error: String?,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = error != null,
            supportingText = { error?.let { Text(it) } }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, content = content)
    }
}

@Composable
private fun DateTimeField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    AppDatePickerField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        error = error,
        includeTime = true
    )
}
