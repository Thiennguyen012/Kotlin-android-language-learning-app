package com.englishlearningapp.ui.screens.flashcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardCollectionDto
import com.englishlearningapp.ui.components.AppTopNavigationBar
import com.englishlearningapp.ui.theme.AppCardBackground
import com.englishlearningapp.ui.theme.AppScreenBackground

private val FlashcardScreenBackground = AppScreenBackground
private val FlashcardCardBackground = AppCardBackground
private val FlashcardAccent = Color(0xFF4968A8)
private val FlashcardSecondaryText = Color(0xFF697386)

@Composable
fun FlashcardCollectionListScreen(
    viewModel: FlashcardCollectionListViewModel,
    onCollectionClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FlashcardScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = "Flashcards",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter flashcard collections"
                        )
                    }
                    IconButton(onClick = viewModel::loadCollections) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        FlashcardCollectionContent(
            uiState = uiState,
            innerPadding = innerPadding,
            onCollectionClick = onCollectionClick,
            onRetryClick = viewModel::loadCollections
        )
    }

    if (showFilterDialog) {
        CollectionFilterDialog(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = {
                viewModel.selectFilter(it)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
private fun FlashcardCollectionContent(
    uiState: CollectionListUiState,
    innerPadding: PaddingValues,
    onCollectionClick: (Int) -> Unit,
    onRetryClick: () -> Unit
) {
    when {
        uiState.isLoading -> ScreenMessageContainer(innerPadding) {
            CircularProgressIndicator(color = FlashcardAccent)
        }

        uiState.errorMessage != null -> ScreenMessageContainer(innerPadding) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetryClick) {
                Text("Retry")
            }
        }

        uiState.collections.isEmpty() -> ScreenMessageContainer(innerPadding) {
            Text(
                text = "No flashcard collections found",
                color = FlashcardSecondaryText,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Your collections",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.collections.size} collections ready to learn",
                        color = FlashcardSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            items(uiState.collections, key = { it.id }) { collection ->
                FlashcardCollectionCard(
                    collection = collection,
                    onClick = { onCollectionClick(collection.id) }
                )
            }
        }
    }
}

@Composable
private fun FlashcardCollectionCard(
    collection: FlashcardCollectionDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FlashcardCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(FlashcardAccent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = collection.collectionName.firstOrNull()?.uppercase() ?: "F",
                    color = FlashcardAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = collection.collectionName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = collection.description?.takeIf { it.isNotBlank() }
                        ?: "Tap to start learning this collection",
                    color = FlashcardSecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = FlashcardSecondaryText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CollectionFilterDialog(
    selectedFilter: CollectionFilter,
    onFilterSelected: (CollectionFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter collections") },
        text = {
            Column {
                CollectionFilter.entries.forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterSelected(filter) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFilter == filter,
                            onClick = { onFilterSelected(filter) }
                        )
                        Text(text = filter.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ScreenMessageContainer(
    innerPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}

private val CollectionFilter.displayName: String
    get() = when (this) {
        CollectionFilter.NEWEST -> "Recently updated"
        CollectionFilter.OLDEST -> "Oldest updated"
        CollectionFilter.NAME_ASCENDING -> "Name A-Z"
    }
