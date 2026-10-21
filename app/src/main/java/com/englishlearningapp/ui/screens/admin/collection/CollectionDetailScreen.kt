package com.englishlearningapp.ui.screens.admin.collection

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun CollectionDetailScreen(
    viewModel: CollectionDetailViewModel,
    onEditClick: (Int) -> Unit,
    onCreateFlashcardClick: (Int) -> Unit,
    onEditFlashcardClick: (collectionId: Int, flashcardId: Int) -> Unit,
    onDeleteSuccess: () -> Unit,
    onBackClick: () -> Unit,
    canViewFlashcards: Boolean,
    canCreateFlashcards: Boolean,
    canUpdateFlashcards: Boolean,
    canUpdateCollection: Boolean,
    canDeleteCollection: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var flashcardIdToDelete by remember { mutableStateOf<Int?>(null) }
    var showAttachDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleteSuccess) {
        if (uiState.isDeleteSuccess) {
            onDeleteSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            if (!uiState.isLoading && uiState.collection != null) {
                snackbarHostState.showSnackbar(it)
            }
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Refresh automatically when returning to this screen (in case of edits)
    LaunchedEffect(Unit) {
        viewModel.loadCollectionDetail()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Collection?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCollection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAttachDialog) {
        CollectionFlashcardPickerDialog(
            flashcards = uiState.availableFlashcards,
            selectedIds = uiState.selectedFlashcardIds,
            isLoading = uiState.isLoadingAvailableFlashcards,
            isSaving = uiState.isUpdatingFlashcards,
            onToggle = viewModel::toggleFlashcardSelection,
            onConfirm = { viewModel.attachSelectedFlashcards { showAttachDialog = false } },
            onDismiss = { showAttachDialog = false }
        )
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Collection Detail",
                onBackClick = onBackClick,
                onRefreshClick = viewModel::loadCollectionDetail,
                actions = {
                    uiState.collection?.let { collection ->
                        if (canUpdateCollection) {
                            IconButton(onClick = {
                                showAttachDialog = true
                                viewModel.loadAvailableFlashcards()
                            }) {
                                Icon(Icons.Default.AddLink, contentDescription = "Attach Flashcards")
                            }
                            IconButton(onClick = { onEditClick(collection.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Collection")
                            }
                        }
                        if (canDeleteCollection) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Collection", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (canCreateFlashcards) uiState.collection?.let { collection ->
                FloatingActionButton(
                    onClick = { onCreateFlashcardClick(collection.id) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Flashcard")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        
        flashcardIdToDelete?.let { flashcardId ->
            com.englishlearningapp.ui.screens.admin.flashcard.DeleteConfirmationDialog(
                title = "Remove Flashcard?",
                message = "Remove this flashcard from the collection? The flashcard will remain available in the system.",
                isDeleting = uiState.isUpdatingFlashcards,
                onConfirm = {
                    viewModel.detachFlashcard(flashcardId)
                    flashcardIdToDelete = null
                },
                onDismiss = { flashcardIdToDelete = null }
            )
        }

        when {
            uiState.isLoading && uiState.collection == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null && uiState.collection == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadCollectionDetail() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.collection != null -> {
                val collection = uiState.collection!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Collection Info Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = collection.collectionName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!collection.description.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = collection.description,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Created: ${formatDate(collection.createdAt)}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Updated: ${formatDate(collection.updatedAt)}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Flashcard Count: ${collection.flashcards.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (canViewFlashcards) item {
                        Text(
                            text = "Flashcards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (canViewFlashcards && collection.flashcards.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No flashcards",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (canViewFlashcards) {
                        items(collection.flashcards) { flashcard ->
                            com.englishlearningapp.ui.screens.admin.flashcard.FlashcardCard(
                                flashcard = flashcard,
                                onEditClick = if (canUpdateFlashcards) ({
                                    onEditFlashcardClick(collection.id, flashcard.id)
                                }) else null,
                                onDeleteClick = if (canUpdateCollection) ({
                                    flashcardIdToDelete = flashcard.id
                                }) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return if (dateString.contains("T")) {
        dateString.substringBefore("T")
    } else {
        dateString
    }
}
