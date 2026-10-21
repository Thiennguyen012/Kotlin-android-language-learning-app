package com.englishlearningapp.ui.screens.admin.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.englishlearningapp.core.permission.PermissionHelper
import com.englishlearningapp.data.remote.dto.auth.UserDto
import com.englishlearningapp.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val user: UserDto? = null,
    val menuItems: List<AdminMenuItem> = emptyList()
)

class DashboardViewModel : ViewModel() {

    private val allMenuItems = listOf(
        AdminMenuItem(
            title = "Flashcard Collections",
            icon = Icons.Default.Style,
            route = Screen.AdminCollectionList.route,
            requiredPermission = "flashcard_collection.view"
        ),
        AdminMenuItem(
            title = "Flashcards",
            icon = Icons.Default.ViewCarousel,
            route = Screen.AdminFlashcardList.route,
            requiredPermission = "flashcard.view"
        ),
        AdminMenuItem(
            title = "Questions",
            icon = Icons.Default.HelpCenter,
            route = Screen.AdminQuestionList.route,
            requiredPermission = "question.view"
        ),
        AdminMenuItem(
            title = "Tests",
            icon = Icons.Default.Quiz,
            route = Screen.AdminTestList.route,
            requiredPermission = "collection_test.view"
        ),
        AdminMenuItem(
            title = "Statistics",
            icon = Icons.Default.Assessment,
            route = null,
            requiredPermission = "user_test_attempt.view",
            isComingSoon = true
        ),
        AdminMenuItem(
            title = "Users",
            icon = Icons.Default.People,
            route = Screen.AdminUserManagement.route,
            requiredPermission = "user.view"
        ),
        AdminMenuItem(
            title = "Permissions",
            icon = Icons.Default.Security,
            route = Screen.AdminPermissionList.route,
            requiredPermission = "permission.view"
        ),
        AdminMenuItem(
            title = "Roles",
            icon = Icons.Default.ManageAccounts,
            route = Screen.AdminRoleList.route,
            requiredPermission = "role.view"
        )
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Set the current user to display their name in the welcome text.
     */
    fun setUser(user: UserDto) {
        val permissionHelper = PermissionHelper(user)
        _uiState.value = _uiState.value.copy(
            user = user,
            menuItems = allMenuItems.filter { menuItem ->
                permissionHelper.checkPermission(menuItem.requiredPermission)
            }
        )
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
