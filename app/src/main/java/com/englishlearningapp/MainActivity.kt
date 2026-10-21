package com.englishlearningapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.englishlearningapp.core.permission.PermissionViewModel
import com.englishlearningapp.core.permission.PermissionHelper
import com.englishlearningapp.ui.navigation.AppBottomNavigation
import com.englishlearningapp.ui.navigation.AppNavGraph
import com.englishlearningapp.ui.navigation.Screen
import com.englishlearningapp.ui.navigation.navigateToLogin
import com.englishlearningapp.ui.theme.EnglishLearningAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the DI container from the Application class
        val appContainer = (application as EnglishLearningApp).container

        setContent {
            EnglishLearningAppTheme {
                // Check if user has a saved token to decide the start screen
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val token = appContainer.tokenManager.getAccessToken()
                    startDestination = if (token.isNullOrEmpty()) {
                        Screen.Login.route
                    } else {
                        Screen.Home.route
                    }
                }

                if (startDestination != null) {
                    val navController = rememberNavController()
                    val permissionViewModel: PermissionViewModel = viewModel(
                        factory = PermissionViewModel.Factory(appContainer.authRepository)
                    )
                    val permissionState by permissionViewModel.uiState.collectAsState()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route
                    val accessToken by appContainer.tokenManager.accessTokenFlow.collectAsState(
                        initial = if (startDestination == Screen.Home.route) "" else null
                    )
                    val showBottomNavigation = currentRoute != null &&
                        currentRoute != Screen.Login.route &&
                        currentRoute != Screen.Register.route

                    LaunchedEffect(accessToken, currentRoute) {
                        if (accessToken == null &&
                            currentRoute != null &&
                            currentRoute != Screen.Login.route &&
                            currentRoute != Screen.Register.route
                        ) {
                            navController.navigateToLogin()
                        }
                    }

                    Scaffold(
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            if (showBottomNavigation) {
                                AppBottomNavigation(
                                    navController = navController,
                                    currentRoute = currentRoute,
                                    hasPermission = { permissionName ->
                                        PermissionHelper(permissionState.user)
                                            .checkPermission(permissionName)
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        AppNavGraph(
                            navController = navController,
                            appContainer = appContainer,
                            startDestination = startDestination!!,
                            permissionViewModel = permissionViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    // Show a loading indicator while checking token
                    LoadingScreen()
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
