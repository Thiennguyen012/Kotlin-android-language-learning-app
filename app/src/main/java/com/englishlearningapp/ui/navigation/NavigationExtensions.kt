package com.englishlearningapp.ui.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToLogin() {
    if (currentDestination?.route == Screen.Login.route) return

    navigate(Screen.Login.route) {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
