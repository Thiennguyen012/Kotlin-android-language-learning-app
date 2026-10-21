package com.englishlearningapp.ui.screens.admin.dashboard

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a clickable menu item in the Admin Dashboard grid.
 */
data class AdminMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String?,
    val requiredPermission: String,
    val isComingSoon: Boolean = false
)
