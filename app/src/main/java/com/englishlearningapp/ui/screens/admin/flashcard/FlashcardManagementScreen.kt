package com.englishlearningapp.ui.screens.admin.flashcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.components.AppSearchField

@Composable
fun FlashcardManagementScreen(
    viewModel: FlashcardManagementViewModel,
    onCreateClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    canCreate: Boolean,
    canUpdate: Boolean,
    canDelete: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Manage Flashcards",
                onBackClick = onBackClick,
                onRefreshClick = viewModel::refresh
            )
        },
        floatingActionButton = {
            if (canCreate) {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "Create flashcard")
            }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppSearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                placeholder = "Search flashcards",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                onClear = viewModel::clearSearch,
                onSearch = {
                    viewModel.applySearch()
                    keyboardController?.hide()
                }
            )

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                uiState.flashcards.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (uiState.appliedSearch.isBlank()) {
                                "No flashcards found"
                            } else {
                                "No flashcards match your search"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.appliedSearch.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = viewModel::clearSearch) { Text("Clear search") }
                        }
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.totalItems} flashcards",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(uiState.flashcards, key = { it.id }) { flashcard ->
                        FlashcardCard(
                            flashcard = flashcard,
                            onEditClick = if (canUpdate) ({ onEditClick(flashcard.id) }) else null,
                            onDeleteClick = if (canDelete) ({ viewModel.requestDelete(flashcard) }) else null
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = viewModel::previousPage,
                                enabled = uiState.currentPage > 1
                            ) { Text("Previous") }
                            Text("Page ${uiState.currentPage} / ${uiState.lastPage}")
                            OutlinedButton(
                                onClick = viewModel::nextPage,
                                enabled = uiState.currentPage < uiState.lastPage
                            ) { Text("Next") }
                        }
                    }
                }
            }
        }
    }

    uiState.deletingFlashcard?.let { flashcard ->
        DeleteConfirmationDialog(
            title = "Delete flashcard",
            message = "Delete '${flashcard.originalWord}'? This action cannot be undone.",
            isDeleting = uiState.isDeleting,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete
        )
    }
}
