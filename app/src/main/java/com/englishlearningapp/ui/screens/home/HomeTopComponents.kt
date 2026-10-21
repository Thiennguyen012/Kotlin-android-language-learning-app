package com.englishlearningapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import com.englishlearningapp.R
import com.englishlearningapp.ui.components.AppSearchField

@Composable
internal fun HomeHeader(
    userName: String,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyMedium,
                color = HomeColors.SecondaryText
            )
            Text(
                text = if (userName.isBlank()) "English learner 👋" else "$userName 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HomeColors.PrimaryText
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoundActionButton(
                icon = Icons.Default.Person,
                contentDescription = "Open profile",
                onClick = onProfileClick
            )
            RoundActionButton(
                icon = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                onClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun RoundActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color.White)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = HomeColors.PrimaryText
        )
    }
}

@Composable
internal fun HomeSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    AppSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = "Search modules..."
    )
}

@Composable
internal fun HomeRecommendations(
    showFlashcards: Boolean,
    showQuizzes: Boolean,
    onFlashcardsClick: () -> Unit,
    onQuizzesClick: () -> Unit
) {
    val recommendations = buildList {
        if (showFlashcards) add(Recommendation(
            title = "Grow your vocabulary",
            subtitle = "Review flashcard collections",
            badge = "Flashcards",
            icon = Icons.Default.Style,
            colors = listOf(Color(0xFFDDECF4), Color(0xFFCDE2ED)),
            backgroundRes = R.drawable.home_flashcard_vocabulary,
            onClick = onFlashcardsClick
        ))
        if (showQuizzes) add(Recommendation(
            title = "Test your knowledge",
            subtitle = "Take a quiz and track progress",
            badge = "Quizzes",
            icon = Icons.Default.Quiz,
            colors = listOf(Color(0xFFE7E0F7), Color(0xFFD8CEF0)),
            backgroundRes = R.drawable.home_quiz_study,
            onClick = onQuizzesClick
        ))
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Recommendations",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(recommendations, key = { it.title }) { recommendation ->
                RecommendationCard(recommendation = recommendation)
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: Recommendation) {
    Card(
        onClick = recommendation.onClick,
        modifier = Modifier
            .width(268.dp)
            .height(190.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(Brush.linearGradient(recommendation.colors))
        ) {
            recommendation.backgroundRes?.let { backgroundRes ->
                Image(
                    painter = painterResource(backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.68f))
                            )
                        )
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(18.dp)
            ) {
                Text(
                    text = recommendation.badge,
                    style = MaterialTheme.typography.labelMedium,
                    color = HomeColors.SecondaryText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (recommendation.backgroundRes != null) Color.White
                    else HomeColors.PrimaryText
                )
                Text(
                    text = recommendation.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (recommendation.backgroundRes != null) Color.White.copy(alpha = 0.82f)
                    else HomeColors.SecondaryText
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = recommendation.icon,
                    contentDescription = null,
                    tint = HomeColors.PrimaryText,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private data class Recommendation(
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val colors: List<Color>,
    @DrawableRes val backgroundRes: Int? = null,
    val onClick: () -> Unit
)
