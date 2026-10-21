package com.englishlearningapp.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.englishlearningapp.di.AppContainer
import com.englishlearningapp.core.permission.PermissionViewModel
import com.englishlearningapp.ui.components.PermissionGuard
import com.englishlearningapp.ui.screens.auth.LoginScreen
import com.englishlearningapp.ui.screens.auth.LoginViewModel
import com.englishlearningapp.ui.screens.auth.RegisterScreen
import com.englishlearningapp.ui.screens.auth.RegisterViewModel
import com.englishlearningapp.ui.screens.admin.dashboard.AdminDashboardScreen
import com.englishlearningapp.ui.screens.admin.dashboard.DashboardViewModel
import com.englishlearningapp.ui.screens.admin.users.UserManagementScreen
import com.englishlearningapp.ui.screens.admin.users.UserManagementViewModel
import com.englishlearningapp.ui.screens.admin.users.UserDetailScreen
import com.englishlearningapp.ui.screens.admin.users.UserDetailViewModel
import com.englishlearningapp.ui.screens.admin.users.UserFormScreen
import com.englishlearningapp.ui.screens.admin.users.UserFormViewModel
import com.englishlearningapp.ui.screens.admin.collection.CollectionDetailScreen
import com.englishlearningapp.ui.screens.admin.collection.CollectionDetailViewModel
import com.englishlearningapp.ui.screens.admin.collection.CollectionListScreen
import com.englishlearningapp.ui.screens.admin.collection.CollectionListViewModel
import com.englishlearningapp.ui.screens.admin.collection.CreateCollectionScreen
import com.englishlearningapp.ui.screens.admin.collection.CreateEditCollectionViewModel
import com.englishlearningapp.ui.screens.admin.collection.EditCollectionScreen
import com.englishlearningapp.ui.screens.admin.flashcard.FlashcardFormScreen
import com.englishlearningapp.ui.screens.admin.flashcard.FlashcardFormViewModel
import com.englishlearningapp.ui.screens.admin.flashcard.FlashcardManagementScreen
import com.englishlearningapp.ui.screens.admin.flashcard.FlashcardManagementViewModel
import com.englishlearningapp.ui.screens.admin.question.QuestionFormScreen
import com.englishlearningapp.ui.screens.admin.question.QuestionFormViewModel
import com.englishlearningapp.ui.screens.admin.question.QuestionManagementScreen
import com.englishlearningapp.ui.screens.admin.question.QuestionManagementViewModel
import com.englishlearningapp.ui.screens.admin.permission.PermissionFormScreen
import com.englishlearningapp.ui.screens.admin.permission.PermissionFormViewModel
import com.englishlearningapp.ui.screens.admin.permission.PermissionManagementScreen
import com.englishlearningapp.ui.screens.admin.permission.PermissionManagementViewModel
import com.englishlearningapp.ui.screens.admin.role.RoleFormScreen
import com.englishlearningapp.ui.screens.admin.role.RoleFormViewModel
import com.englishlearningapp.ui.screens.admin.role.RoleManagementScreen
import com.englishlearningapp.ui.screens.admin.role.RoleManagementViewModel
import com.englishlearningapp.ui.screens.admin.test.TestFormScreen
import com.englishlearningapp.ui.screens.admin.test.TestFormViewModel
import com.englishlearningapp.ui.screens.admin.test.TestManagementScreen
import com.englishlearningapp.ui.screens.admin.test.TestManagementViewModel
import com.englishlearningapp.ui.screens.flashcard.FlashcardCollectionListScreen
import com.englishlearningapp.ui.screens.flashcard.FlashcardCollectionListViewModel
import com.englishlearningapp.ui.screens.flashcard.FlashcardStudyScreen
import com.englishlearningapp.ui.screens.flashcard.FlashcardStudyViewModel
import com.englishlearningapp.ui.screens.home.HomeScreen
import com.englishlearningapp.ui.screens.home.HomeViewModel
import com.englishlearningapp.ui.screens.profile.EditProfileScreen
import com.englishlearningapp.ui.screens.profile.EditProfileViewModel
import com.englishlearningapp.ui.screens.profile.ProfileScreen
import com.englishlearningapp.ui.screens.profile.ProfileViewModel
import com.englishlearningapp.ui.screens.quiz.QuizListScreen
import com.englishlearningapp.ui.screens.quiz.QuizListViewModel
import com.englishlearningapp.ui.screens.quiz.QuizDetailScreen
import com.englishlearningapp.ui.screens.quiz.QuizDetailViewModel
import com.englishlearningapp.ui.screens.quiz.QuizTakingScreen
import com.englishlearningapp.ui.screens.quiz.QuizTakingViewModel
import com.englishlearningapp.ui.screens.quiz.ResultScreen
import com.englishlearningapp.ui.screens.quiz.ResultViewModel
import com.englishlearningapp.ui.screens.quiz.AttemptHistoryScreen
import com.englishlearningapp.ui.screens.quiz.AttemptHistoryViewModel
import com.englishlearningapp.ui.screens.quiz.AttemptDetailScreen
import com.englishlearningapp.ui.screens.quiz.AttemptDetailViewModel
import com.englishlearningapp.ui.screens.speaking.SpeakingPracticeScreen
import com.englishlearningapp.ui.screens.speaking.SpeakingPracticeViewModel
import com.englishlearningapp.ui.screens.speakinglevel.SpeakingLevelScreen
import com.englishlearningapp.ui.screens.speakinglevel.SpeakingLevelViewModel

/**
 * Main navigation graph for the app.
 *
 * @param navController The NavHostController that manages navigation.
 * @param appContainer The DI container for creating ViewModels.
 * @param startDestination The initial screen to show (login or home).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    startDestination: String,
    permissionViewModel: PermissionViewModel,
    modifier: Modifier = Modifier
) {
    val permissionState by permissionViewModel.uiState.collectAsState()

    LaunchedEffect(startDestination) {
        if (startDestination != Screen.Login.route) {
            permissionViewModel.loadPermissions()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(220))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(220))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(220))
        }
    ) {
        // --- Auth ---

        composable(Screen.Login.route) { backStackEntry ->
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(appContainer.authRepository)
            )
            val registrationMessage by backStackEntry.savedStateHandle
                .getStateFlow<String?>("registration_message", null)
                .collectAsState()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    permissionViewModel.loadPermissions(forceRefresh = true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                registrationMessage = registrationMessage,
                onRegistrationMessageShown = {
                    backStackEntry.savedStateHandle["registration_message"] = null
                }
            )
        }

        composable(Screen.Register.route) {
            val registerViewModel: RegisterViewModel = viewModel(
                factory = RegisterViewModel.Factory(appContainer.authRepository)
            )
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = { message ->
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set("registration_message", message)
                    navController.popBackStack()
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // --- Home ---

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(appContainer.authRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onLoggedOut = {
                    navController.navigateToLogin()
                },
                onFlashcardsClick = {
                    navController.navigate(Screen.FlashcardCollections.route)
                },
                onQuizzesClick = {
                    navController.navigate(Screen.QuizList.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onAdminDashboardClick = {
                    navController.navigate(Screen.AdminDashboard.route)
                },
                onSpeakingPracticeClick = {
                    navController.navigate(Screen.SpeakingPractice.route)
                },
                onSpeakingLevelClick = {
                    navController.navigate(Screen.SpeakingLevelAssessment.route)
                }
            )
        }

        composable(Screen.SpeakingPractice.route) {
            val vm: SpeakingPracticeViewModel = viewModel(
                factory = SpeakingPracticeViewModel.Factory(appContainer.speakingAssessmentRepository)
            )
            SpeakingPracticeScreen(vm, onBackClick = { navController.popBackStack() })
        }

        composable(Screen.SpeakingLevelAssessment.route) {
            val vm: SpeakingLevelViewModel = viewModel(
                factory = SpeakingLevelViewModel.Factory(appContainer.speakingLevelAssessmentRepository)
            )
            SpeakingLevelScreen(vm, onBackClick = { navController.popBackStack() })
        }

        // --- Flashcard Flow ---

        composable(Screen.FlashcardCollections.route) {
            PermissionGuard(
                permissionName = "flashcard.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: FlashcardCollectionListViewModel = viewModel(
                    factory = FlashcardCollectionListViewModel.Factory(appContainer.flashcardRepository)
                )
                FlashcardCollectionListScreen(
                    viewModel = vm,
                    onCollectionClick = { collectionId ->
                        navController.navigate(Screen.FlashcardStudy.createRoute(collectionId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.FlashcardStudy.route,
            arguments = listOf(navArgument("collectionId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "flashcard.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val collectionId = backStackEntry.arguments?.getInt("collectionId") ?: return@PermissionGuard
                val vm: FlashcardStudyViewModel = viewModel(
                    factory = FlashcardStudyViewModel.Factory(
                        appContainer.flashcardRepository,
                        collectionId
                    )
                )
                FlashcardStudyScreen(
                    viewModel = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // --- Quiz Flow ---

        composable(Screen.QuizList.route) {
            PermissionGuard(
                permissionName = "collection_test.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: QuizListViewModel = viewModel(
                    factory = QuizListViewModel.Factory(appContainer.quizRepository)
                )
                QuizListScreen(
                    viewModel = vm,
                    onTestClick = { testId ->
                        navController.navigate(Screen.QuizDetail.createRoute(testId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.QuizDetail.route,
            arguments = listOf(navArgument("testId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "collection_test.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val testId = backStackEntry.arguments?.getInt("testId") ?: return@PermissionGuard
                val vm: QuizDetailViewModel = viewModel(
                    factory = QuizDetailViewModel.Factory(appContainer.quizRepository, testId)
                )
                QuizDetailScreen(
                    viewModel = vm,
                    canStartAttempt = permissionViewModel.hasPermission("attempt.do"),
                    canViewAttempt = permissionViewModel.hasPermission("attempt.view"),
                    onStartTest = { selectedTestId ->
                        navController.navigate(Screen.QuizTaking.createRoute(selectedTestId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.QuizTaking.route,
            arguments = listOf(navArgument("testId") { type = NavType.IntType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getInt("testId") ?: return@composable
            PermissionGuard(
                permissionName = "attempt.do",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                PermissionGuard(
                    permissionName = "attempt.view",
                    permissionState = permissionState,
                    hasPermission = permissionViewModel::hasPermission,
                    onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                    onBackClick = { navController.popBackStack() }
                ) {
                    val canAnswer = permissionViewModel.hasPermission("attempt.do")
                    val canSubmit = permissionViewModel.hasPermission("attempt.do")
                    val vm: QuizTakingViewModel = viewModel(
                        factory = QuizTakingViewModel.Factory(
                            appContainer.quizRepository,
                            testId,
                            canAnswer,
                            canSubmit
                        )
                    )
                    QuizTakingScreen(
                        viewModel = vm,
                        canAnswer = canAnswer,
                        canSubmit = canSubmit,
                        onQuizFinished = {
                            val currentRoute = navController.currentDestination?.route
                            navController.navigate(Screen.QuizResult.route) {
                                if (currentRoute != null) {
                                    popUpTo(currentRoute) { inclusive = true }
                                }
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(Screen.QuizResult.route) {
            PermissionGuard(
                permissionName = "attempt.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: ResultViewModel = viewModel(
                    factory = ResultViewModel.Factory(appContainer.questionRepository)
                )
                ResultScreen(
                viewModel = vm,
                onBackToQuizzes = {
                    // Pop back to QuizList (removes ResultScreen from stack)
                    val popped = navController.popBackStack(Screen.QuizList.route, inclusive = false)
                    if (!popped) {
                        // Fallback: navigate to QuizList and pop up to start destination
                        navController.navigate(Screen.QuizList.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                        }
                    }
                }
                )
            }
        }

        composable(Screen.AttemptHistory.route) {
            PermissionGuard(
                permissionName = "attempt.history",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: AttemptHistoryViewModel = viewModel(
                    factory = AttemptHistoryViewModel.Factory(appContainer.quizRepository)
                )
                AttemptHistoryScreen(
                    viewModel = vm,
                    onAttemptClick = { attemptId ->
                        navController.navigate(Screen.AttemptHistoryDetail.createRoute(attemptId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AttemptHistoryDetail.route,
            arguments = listOf(navArgument("attemptId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "attempt.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val attemptId = backStackEntry.arguments?.getInt("attemptId") ?: return@PermissionGuard
                val vm: AttemptDetailViewModel = viewModel(
                    factory = AttemptDetailViewModel.Factory(
                        appContainer.quizRepository,
                        appContainer.questionRepository,
                        attemptId
                    )
                )
                AttemptDetailScreen(
                    viewModel = vm,
                    canContinueAttempt = permissionViewModel.hasPermission("attempt.do"),
                    onContinueTest = { testId ->
                        navController.navigate(Screen.QuizTaking.createRoute(testId)) {
                            popUpTo(Screen.AttemptHistory.route)
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // --- Profile Flow ---

        composable(Screen.Profile.route) { backStackEntry ->
            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(
                    appContainer.profileRepository,
                    appContainer.authRepository
                )
            )
            val profileChanged by backStackEntry.savedStateHandle
                .getStateFlow("profile_changed", false)
                .collectAsState()
            LaunchedEffect(profileChanged) {
                if (profileChanged) {
                    vm.loadProfile()
                    permissionViewModel.loadPermissions(forceRefresh = true)
                    backStackEntry.savedStateHandle["profile_changed"] = false
                }
            }
            ProfileScreen(
                viewModel = vm,
                onEditClick = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onLoggedOut = {
                    navController.navigateToLogin()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            val vm: EditProfileViewModel = viewModel(
                factory = EditProfileViewModel.Factory(appContainer.profileRepository)
            )

            // Pre-fill form with data from the previous ProfileScreen's back stack
            val profileEntry = navController.previousBackStackEntry
            val profileVm = profileEntry?.let {
                // Try to get the ProfileViewModel from the previous entry
                // If unavailable, the form will start empty and user can still type
                try {
                    ViewModelProvider(it, ProfileViewModel.Factory(
                        appContainer.profileRepository,
                        appContainer.authRepository
                    ))[ProfileViewModel::class.java]
                } catch (_: Exception) { null }
            }

            // Initialize form with existing profile data
            profileVm?.uiState?.value?.user?.let { user ->
                vm.initializeForm(
                    name = user.name,
                    phone = user.phone ?: "",
                    birthday = formatBirthdayForEdit(user.birthday),
                    address = user.address ?: "",
                    email = user.email,
                    avatarPath = user.avatar
                )
            }

            EditProfileScreen(
                viewModel = vm,
                onSaveSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set("profile_changed", true)
                    navController.popBackStack()
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }

        // --- Admin Flow ---

        composable(Screen.AdminDashboard.route) {
            PermissionGuard(
                permissionName = "admin_dashboard.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory()
                )
                val homeEntry = navController.previousBackStackEntry
                val homeVm = homeEntry?.let {
                    try {
                        ViewModelProvider(
                            it,
                            HomeViewModel.Factory(appContainer.authRepository)
                        )[HomeViewModel::class.java]
                    } catch (_: Exception) {
                        null
                    }
                }
                homeVm?.uiState?.value?.user?.let(vm::setUser)

                AdminDashboardScreen(
                    viewModel = vm,
                    onNavigateToRoute = navController::navigate,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminUserManagement.route) { backStackEntry ->
            PermissionGuard(
                permissionName = "user.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: UserManagementViewModel = viewModel(
                    factory = UserManagementViewModel.Factory(appContainer.userRepository)
                )
                val userChanged by backStackEntry.savedStateHandle
                    .getStateFlow("user_changed", false)
                    .collectAsState()
                LaunchedEffect(userChanged) {
                    if (userChanged) {
                        vm.refresh()
                        backStackEntry.savedStateHandle["user_changed"] = false
                    }
                }
                UserManagementScreen(
                    viewModel = vm,
                    canCreate = permissionViewModel.hasPermission("user.create"),
                    canUpdate = permissionViewModel.hasPermission("user.update"),
                    canDelete = permissionViewModel.hasPermission("user.delete"),
                    onUserClick = { id -> navController.navigate(Screen.AdminUserDetail.createRoute(id)) },
                    onCreateClick = { navController.navigate(Screen.AdminUserCreate.route) },
                    onEditClick = { id -> navController.navigate(Screen.AdminUserEdit.createRoute(id)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AdminUserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "user.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val userId = backStackEntry.arguments?.getInt("userId") ?: return@PermissionGuard
                val vm: UserDetailViewModel = viewModel(
                    factory = UserDetailViewModel.Factory(appContainer.userRepository, userId)
                )
                val userDetailChanged by backStackEntry.savedStateHandle
                    .getStateFlow("user_detail_changed", false)
                    .collectAsState()
                LaunchedEffect(userDetailChanged) {
                    if (userDetailChanged) {
                        vm.refresh()
                        backStackEntry.savedStateHandle["user_detail_changed"] = false
                    }
                }
                UserDetailScreen(
                    viewModel = vm,
                    canUpdate = permissionViewModel.hasPermission("user.update"),
                    onEditClick = { id -> navController.navigate(Screen.AdminUserEdit.createRoute(id)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminUserCreate.route) {
            PermissionGuard(
                permissionName = "user.create",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: UserFormViewModel = viewModel(
                    factory = UserFormViewModel.Factory(
                        appContainer.userRepository,
                        appContainer.roleRepository,
                        null
                    )
                )
                UserFormScreen(
                    viewModel = vm,
                    isEditMode = false,
                    onSaveSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("user_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AdminUserEdit.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "user.update",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val userId = backStackEntry.arguments?.getInt("userId") ?: return@PermissionGuard
                val vm: UserFormViewModel = viewModel(
                    factory = UserFormViewModel.Factory(
                        appContainer.userRepository,
                        appContainer.roleRepository,
                        userId
                    )
                )
                UserFormScreen(
                    viewModel = vm,
                    isEditMode = true,
                    onSaveSuccess = {
                        runCatching {
                            navController.getBackStackEntry(Screen.AdminUserManagement.route)
                                .savedStateHandle["user_changed"] = true
                        }
                        navController.previousBackStackEntry?.savedStateHandle
                            ?.set("user_detail_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminPermissionList.route) { backStackEntry ->
            PermissionGuard(
                permissionName = "permission.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: PermissionManagementViewModel = viewModel(
                    factory = PermissionManagementViewModel.Factory(appContainer.permissionRepository)
                )
                val permissionChanged by backStackEntry.savedStateHandle
                    .getStateFlow("permission_changed", false)
                    .collectAsState()
                LaunchedEffect(permissionChanged) {
                    if (permissionChanged) {
                        vm.refresh()
                        permissionViewModel.loadPermissions(forceRefresh = true)
                        backStackEntry.savedStateHandle["permission_changed"] = false
                    }
                }
                PermissionManagementScreen(
                    viewModel = vm,
                    canCreate = permissionViewModel.hasPermission("permission.create"),
                    canUpdate = permissionViewModel.hasPermission("permission.update"),
                    canDelete = permissionViewModel.hasPermission("permission.delete"),
                    onCreateClick = { navController.navigate(Screen.AdminPermissionCreate.route) },
                    onEditClick = { id ->
                        navController.navigate(Screen.AdminPermissionEdit.createRoute(id))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminPermissionCreate.route) {
            PermissionGuard(
                permissionName = "permission.create",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: PermissionFormViewModel = viewModel(
                    factory = PermissionFormViewModel.Factory(
                        repository = appContainer.permissionRepository,
                        permissionId = null,
                        initialPermission = null
                    )
                )
                PermissionFormScreen(
                    viewModel = vm,
                    isEditMode = false,
                    onSaveSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle
                            ?.set("permission_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AdminPermissionEdit.route,
            arguments = listOf(navArgument("permissionId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "permission.update",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val permissionId = backStackEntry.arguments?.getInt("permissionId")
                    ?: return@PermissionGuard
                val listEntry = navController.previousBackStackEntry
                val initialPermission = listEntry?.let { entry ->
                    runCatching {
                        ViewModelProvider(
                            entry,
                            PermissionManagementViewModel.Factory(appContainer.permissionRepository)
                        )[PermissionManagementViewModel::class.java].findPermission(permissionId)
                    }.getOrNull()
                }
                val vm: PermissionFormViewModel = viewModel(
                    factory = PermissionFormViewModel.Factory(
                        repository = appContainer.permissionRepository,
                        permissionId = permissionId,
                        initialPermission = initialPermission
                    )
                )
                PermissionFormScreen(
                    viewModel = vm,
                    isEditMode = true,
                    onSaveSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle
                            ?.set("permission_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminRoleList.route) { backStackEntry ->
            PermissionGuard(
                permissionName = "role.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: RoleManagementViewModel = viewModel(
                    factory = RoleManagementViewModel.Factory(appContainer.roleRepository)
                )
                val roleChanged by backStackEntry.savedStateHandle
                    .getStateFlow("role_changed", false)
                    .collectAsState()
                LaunchedEffect(roleChanged) {
                    if (roleChanged) {
                        vm.refresh()
                        backStackEntry.savedStateHandle["role_changed"] = false
                    }
                }
                RoleManagementScreen(
                    viewModel = vm,
                    canCreate = permissionViewModel.hasPermission("role.create"),
                    canUpdate = permissionViewModel.hasPermission("role.update"),
                    canDelete = permissionViewModel.hasPermission("role.delete"),
                    onCreateClick = { navController.navigate(Screen.AdminRoleCreate.route) },
                    onEditClick = { id -> navController.navigate(Screen.AdminRoleEdit.createRoute(id)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminRoleCreate.route) {
            PermissionGuard(
                permissionName = "role.create",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: RoleFormViewModel = viewModel(
                    factory = RoleFormViewModel.Factory(
                        roleRepository = appContainer.roleRepository,
                        permissionRepository = appContainer.permissionRepository,
                        roleId = null,
                        initialRole = null
                    )
                )
                RoleFormScreen(
                    viewModel = vm,
                    isEditMode = false,
                    onSaveSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("role_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AdminRoleEdit.route,
            arguments = listOf(navArgument("roleId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "role.update",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val roleId = backStackEntry.arguments?.getInt("roleId") ?: return@PermissionGuard
                val listEntry = navController.previousBackStackEntry
                val initialRole = listEntry?.let { entry ->
                    runCatching {
                        ViewModelProvider(
                            entry,
                            RoleManagementViewModel.Factory(appContainer.roleRepository)
                        )[RoleManagementViewModel::class.java].findRole(roleId)
                    }.getOrNull()
                }
                val vm: RoleFormViewModel = viewModel(
                    factory = RoleFormViewModel.Factory(
                        roleRepository = appContainer.roleRepository,
                        permissionRepository = appContainer.permissionRepository,
                        roleId = roleId,
                        initialRole = initialRole
                    )
                )
                RoleFormScreen(
                    viewModel = vm,
                    isEditMode = true,
                    onSaveSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("role_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminCollectionList.route) {
            PermissionGuard(
                permissionName = "flashcard_collection.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: CollectionListViewModel = viewModel(
                    factory = CollectionListViewModel.Factory(appContainer.collectionRepository)
                )
                CollectionListScreen(
                    viewModel = vm,
                    onCollectionClick = { id ->
                        navController.navigate(Screen.AdminCollectionDetail.createRoute(id))
                    },
                    onCreateClick = {
                        navController.navigate(Screen.AdminCreateCollection.route)
                    },
                    onBackClick = { navController.popBackStack() },
                    canCreate = permissionViewModel.hasPermission("flashcard_collection.create")
                )
            }
        }

        composable(
            route = Screen.AdminCollectionDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "flashcard_collection.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val id = backStackEntry.arguments?.getInt("id") ?: return@PermissionGuard
                val vm: CollectionDetailViewModel = viewModel(
                    factory = CollectionDetailViewModel.Factory(appContainer.collectionRepository, appContainer.flashcardRepository, id)
                )
                CollectionDetailScreen(
                viewModel = vm,
                onEditClick = { collectionId ->
                    navController.navigate(Screen.AdminEditCollection.createRoute(collectionId))
                },
                onCreateFlashcardClick = { collectionId ->
                    navController.navigate(Screen.AdminFlashcardCreate.createRoute(collectionId))
                },
                onEditFlashcardClick = { collectionId, flashcardId ->
                    navController.navigate(Screen.AdminFlashcardEdit.createRoute(collectionId, flashcardId))
                },
                onDeleteSuccess = {
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() },
                canViewFlashcards = permissionViewModel.hasPermission("flashcard.view"),
                canCreateFlashcards = permissionViewModel.hasPermission("flashcard.create"),
                canUpdateFlashcards = permissionViewModel.hasPermission("flashcard.update"),
                    canUpdateCollection = permissionViewModel.hasPermission("flashcard_collection.update"),
                    canDeleteCollection = permissionViewModel.hasPermission("flashcard_collection.delete")
                )
            }
        }

        composable(Screen.AdminCreateCollection.route) {
            PermissionGuard(
                permissionName = "flashcard_collection.create",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: CreateEditCollectionViewModel = viewModel(
                    factory = CreateEditCollectionViewModel.Factory(appContainer.collectionRepository, null)
                )
                CreateCollectionScreen(
                viewModel = vm,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onCancelClick = {
                    navController.popBackStack()
                }
                )
            }
        }

        composable(Screen.AdminFlashcardList.route) { backStackEntry ->
            PermissionGuard(
                permissionName = "flashcard.view",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: FlashcardManagementViewModel = viewModel(
                    factory = FlashcardManagementViewModel.Factory(appContainer.flashcardRepository)
                )
                val flashcardChanged by backStackEntry.savedStateHandle
                    .getStateFlow("flashcard_changed", false)
                    .collectAsState()
                LaunchedEffect(flashcardChanged) {
                    if (flashcardChanged) {
                        vm.refresh()
                        backStackEntry.savedStateHandle["flashcard_changed"] = false
                    }
                }
                FlashcardManagementScreen(
                    viewModel = vm,
                    onCreateClick = {
                        navController.navigate(Screen.AdminStandaloneFlashcardCreate.route)
                    },
                    onEditClick = { flashcardId ->
                        navController.navigate(Screen.AdminStandaloneFlashcardEdit.createRoute(flashcardId))
                    },
                    onBackClick = { navController.popBackStack() },
                    canCreate = permissionViewModel.hasPermission("flashcard.create"),
                    canUpdate = permissionViewModel.hasPermission("flashcard.update"),
                    canDelete = permissionViewModel.hasPermission("flashcard.delete")
                )
            }
        }

        composable(Screen.AdminStandaloneFlashcardCreate.route) {
            PermissionGuard(
                permissionName = "flashcard.create",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val vm: FlashcardFormViewModel = viewModel(
                    factory = FlashcardFormViewModel.Factory(appContainer.flashcardRepository, null, null)
                )
                FlashcardFormScreen(
                    viewModel = vm,
                    isEditMode = false,
                    onSaveSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("flashcard_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminQuestionList.route) { backStackEntry ->
            val vm: QuestionManagementViewModel = viewModel(
                factory = QuestionManagementViewModel.Factory(appContainer.questionRepository)
            )
            val questionChanged by backStackEntry.savedStateHandle
                .getStateFlow("question_changed", false)
                .collectAsState()
            LaunchedEffect(questionChanged) {
                if (questionChanged) {
                    vm.refresh()
                    backStackEntry.savedStateHandle["question_changed"] = false
                }
            }
            QuestionManagementScreen(
                viewModel = vm,
                onCreateClick = { navController.navigate(Screen.AdminQuestionCreate.route) },
                onEditClick = { id -> navController.navigate(Screen.AdminQuestionEdit.createRoute(id)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminQuestionCreate.route) {
            val vm: QuestionFormViewModel = viewModel(
                factory = QuestionFormViewModel.Factory(appContainer.questionRepository, null)
            )
            QuestionFormScreen(
                viewModel = vm,
                isEditMode = false,
                onSaveSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("question_changed", true)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminTestList.route) { backStackEntry ->
            val vm: TestManagementViewModel = viewModel(
                factory = TestManagementViewModel.Factory(appContainer.quizRepository)
            )
            val testChanged by backStackEntry.savedStateHandle
                .getStateFlow("test_changed", false)
                .collectAsState()
            LaunchedEffect(testChanged) {
                if (testChanged) {
                    vm.refresh()
                    backStackEntry.savedStateHandle["test_changed"] = false
                }
            }
            TestManagementScreen(
                viewModel = vm,
                onCreateClick = { navController.navigate(Screen.AdminTestCreate.route) },
                onEditClick = { id -> navController.navigate(Screen.AdminTestEdit.createRoute(id)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminTestCreate.route) {
            val vm: TestFormViewModel = viewModel(
                factory = TestFormViewModel.Factory(
                    appContainer.quizRepository,
                    appContainer.flashcardRepository,
                    appContainer.questionRepository,
                    null
                )
            )
            TestFormScreen(
                viewModel = vm,
                isEditMode = false,
                onSaveSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("test_changed", true)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AdminTestEdit.route,
            arguments = listOf(navArgument("testId") { type = NavType.IntType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getInt("testId") ?: return@composable
            val vm: TestFormViewModel = viewModel(
                factory = TestFormViewModel.Factory(
                    appContainer.quizRepository,
                    appContainer.flashcardRepository,
                    appContainer.questionRepository,
                    testId
                )
            )
            TestFormScreen(
                viewModel = vm,
                isEditMode = true,
                onSaveSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("test_changed", true)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AdminQuestionEdit.route,
            arguments = listOf(navArgument("questionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getInt("questionId") ?: return@composable
            val vm: QuestionFormViewModel = viewModel(
                factory = QuestionFormViewModel.Factory(appContainer.questionRepository, questionId)
            )
            QuestionFormScreen(
                viewModel = vm,
                isEditMode = true,
                onSaveSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("question_changed", true)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AdminStandaloneFlashcardEdit.route,
            arguments = listOf(navArgument("flashcardId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "flashcard.update",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val flashcardId = backStackEntry.arguments?.getInt("flashcardId") ?: return@PermissionGuard
                val vm: FlashcardFormViewModel = viewModel(
                    factory = FlashcardFormViewModel.Factory(appContainer.flashcardRepository, null, flashcardId)
                )
                FlashcardFormScreen(
                    viewModel = vm,
                    isEditMode = true,
                    onSaveSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("flashcard_changed", true)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AdminEditCollection.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "flashcard_collection.update",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val id = backStackEntry.arguments?.getInt("id") ?: return@PermissionGuard
                val vm: CreateEditCollectionViewModel = viewModel(
                    factory = CreateEditCollectionViewModel.Factory(appContainer.collectionRepository, id)
                )
                EditCollectionScreen(
                viewModel = vm,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onCancelClick = {
                    navController.popBackStack()
                }
                )
            }
        }

        composable(
            route = Screen.AdminFlashcardCreate.route,
            arguments = listOf(navArgument("collectionId") { type = NavType.IntType })
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "flashcard.create",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val collectionId = backStackEntry.arguments?.getInt("collectionId") ?: return@PermissionGuard
                val vm: FlashcardFormViewModel = viewModel(
                    factory = FlashcardFormViewModel.Factory(appContainer.flashcardRepository, collectionId, null)
                )
                FlashcardFormScreen(
                    viewModel = vm,
                    isEditMode = false,
                    onSaveSuccess = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.AdminFlashcardEdit.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.IntType },
                navArgument("flashcardId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            PermissionGuard(
                permissionName = "flashcard.update",
                permissionState = permissionState,
                hasPermission = permissionViewModel::hasPermission,
                onRetry = { permissionViewModel.loadPermissions(forceRefresh = true) },
                onBackClick = { navController.popBackStack() }
            ) {
                val collectionId = backStackEntry.arguments?.getInt("collectionId") ?: return@PermissionGuard
                val flashcardId = backStackEntry.arguments?.getInt("flashcardId") ?: return@PermissionGuard
                val vm: FlashcardFormViewModel = viewModel(
                    factory = FlashcardFormViewModel.Factory(appContainer.flashcardRepository, collectionId, flashcardId)
                )
                FlashcardFormScreen(
                    viewModel = vm,
                    isEditMode = true,
                    onSaveSuccess = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Extract date portion from ISO birthday string for the edit form.
 * e.g., "1989-12-31T17:00:00.000000Z" -> "1989-12-31"
 */
private fun formatBirthdayForEdit(birthday: String?): String {
    if (birthday.isNullOrEmpty()) return ""
    return if (birthday.contains("T")) {
        birthday.substringBefore("T")
    } else {
        birthday
    }
}
