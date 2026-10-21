package com.englishlearningapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal enum class HomeCategory(val label: String) {
    ALL("Popular"),
    LEARN("Learn"),
    TEST("Test"),
    ACCOUNT("Account"),
    ADMIN("Admin")
}

internal data class HomeModule(
    val title: String,
    val description: String,
    val category: HomeCategory,
    val icon: ImageVector,
    val onClick: () -> Unit
)

internal object HomeColors {
    val Background = Color(0xFFF7F7F7)
    val PrimaryText = Color(0xFF17191C)
    val SecondaryText = Color(0xFF71767F)
    val AccentBlue = Color(0xFF9DB7F4)
}

@Composable
internal fun HomeCategorySelector(
    selectedCategory: HomeCategory,
    showLearnCategory: Boolean,
    showTestCategory: Boolean,
    showAdminCategory: Boolean,
    onCategorySelected: (HomeCategory) -> Unit
) {
    val categories = HomeCategory.entries.filter { category ->
        when (category) {
            HomeCategory.LEARN -> showLearnCategory
            HomeCategory.TEST -> showTestCategory
            HomeCategory.ADMIN -> showAdminCategory
            else -> true
        }
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(categories, key = { it.name }) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category.label) },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = HomeColors.AccentBlue,
                    labelColor = HomeColors.SecondaryText,
                    selectedLabelColor = HomeColors.PrimaryText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
internal fun HomeModuleCard(module: HomeModule) {
    Card(
        onClick = module.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E7EA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF4F5F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = HomeColors.PrimaryText
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeColors.PrimaryText
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = HomeColors.SecondaryText,
                    maxLines = 2
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open ${module.title}",
                tint = HomeColors.PrimaryText
            )
        }
    }
}

@Composable
internal fun EmptyModuleResult() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No matching modules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Try another keyword or category.",
                style = MaterialTheme.typography.bodyMedium,
                color = HomeColors.SecondaryText
            )
        }
    }
}

@Composable
internal fun HomeErrorMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
