package com.englishlearningapp.ui.screens.flashcard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishlearningapp.data.remote.dto.flashcard.FlashcardDto
import com.englishlearningapp.ui.components.AppTopNavigationBar

@Composable
fun FlashcardStudyScreen(
    viewModel: FlashcardStudyViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var detailsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.currentIndex) {
        detailsExpanded = false
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopNavigationBar(
                title = uiState.collectionName.ifEmpty { "Flashcards" },
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> StudyStateContainer(Modifier.padding(innerPadding)) {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> StudyStateContainer(Modifier.padding(innerPadding)) {
                Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBackClick) { Text("Back to collections") }
            }

            uiState.flashcards.isEmpty() -> StudyStateContainer(Modifier.padding(innerPadding)) {
                Text("No flashcards in this collection")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBackClick) { Text("Back to collections") }
            }

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
            ) {
                Text(
                    text = uiState.progress,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (uiState.currentIndex + 1).toFloat() / uiState.flashcards.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                uiState.currentFlashcard?.let { flashcard ->
                    FlashcardStudyCard(
                        flashcard = flashcard,
                        isFlipped = uiState.isFlipped,
                        detailsExpanded = detailsExpanded,
                        currentIndex = uiState.currentIndex,
                        totalCount = uiState.flashcards.size,
                        canGoPrevious = uiState.canGoPrevious,
                        canGoNext = uiState.canGoNext,
                        onFlip = viewModel::flipCard,
                        onToggleDetails = { detailsExpanded = !detailsExpanded },
                        onPrevious = viewModel::previousCard,
                        onNext = viewModel::nextCard,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardStudyCard(
    flashcard: FlashcardDto,
    isFlipped: Boolean,
    detailsExpanded: Boolean,
    currentIndex: Int,
    totalCount: Int,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onFlip: () -> Unit,
    onToggleDetails: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primary) {
                Text(
                    text = "Flashcard ${currentIndex + 1}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                )
            }

            FlashcardFlipArea(
                flashcard = flashcard,
                isFlipped = isFlipped,
                onFlip = onFlip,
                modifier = Modifier.weight(1f)
            )

            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleDetails),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Details", fontWeight = FontWeight.SemiBold)
                    Icon(
                        imageVector = if (detailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (detailsExpanded) "Hide details" else "Show details"
                    )
                }
            }

            AnimatedVisibility(visible = detailsExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    FlashcardDetailRow("Original", flashcard.originalWord)
                    FlashcardDetailRow("Translated", flashcard.translatedWord)
                    FlashcardDetailRow("Word type", flashcard.wordType?.displayValue ?: "Not set")
                    FlashcardDetailRow(
                        "Explanation",
                        flashcard.explanation?.takeIf { it.isNotBlank() } ?: "Not set"
                    )
                }
            }

            FlashcardNavigation(
                currentIndex = currentIndex,
                totalCount = totalCount,
                canGoPrevious = canGoPrevious,
                canGoNext = canGoNext,
                onPrevious = onPrevious,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun FlashcardFlipArea(
    flashcard: FlashcardDto,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isFlipped) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
            .clickable(onClick = onFlip),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isFlipped,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "flashcard_flip"
        ) { flipped ->
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (flipped) flashcard.translatedWord else flashcard.originalWord,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (flipped) "Translation" else "Original",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text("Tap to flip", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun FlashcardNavigation(
    currentIndex: Int,
    totalCount: Int,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoPrevious,
            modifier = Modifier.weight(1f).height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
            Text("Previous")
        }
        Button(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier.weight(1f).height(52.dp)
        ) {
            Text("Next")
            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally)
    ) {
        repeat(totalCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentIndex) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentIndex) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun FlashcardDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun StudyStateContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}
