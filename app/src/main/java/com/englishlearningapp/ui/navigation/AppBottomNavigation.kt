package com.englishlearningapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

private data class BottomNavigationItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val relatedRoutes: Set<String>,
    val requiredPermission: String? = null
)

private val bottomNavigationItems = listOf(
    BottomNavigationItem(
        label = "Home",
        route = Screen.Home.route,
        icon = Icons.Default.Home,
        relatedRoutes = setOf(Screen.Home.route)
    ),
    BottomNavigationItem(
        label = "Flashcards",
        route = Screen.FlashcardCollections.route,
        icon = Icons.Default.Style,
        relatedRoutes = setOf(
            Screen.FlashcardCollections.route,
            Screen.FlashcardStudy.route
        ),
        requiredPermission = "flashcard.view"
    ),
    BottomNavigationItem(
        label = "Quizzes",
        route = Screen.QuizList.route,
        icon = Icons.Default.Quiz,
        relatedRoutes = setOf(
            Screen.QuizList.route,
            Screen.QuizDetail.route,
            Screen.QuizTaking.route,
            Screen.QuizResult.route
        ),
        requiredPermission = "collection_test.view"
    ),
    BottomNavigationItem(
        label = "History",
        route = Screen.AttemptHistory.route,
        icon = Icons.Default.History,
        relatedRoutes = setOf(
            Screen.AttemptHistory.route,
            Screen.AttemptHistoryDetail.route
        ),
        requiredPermission = "attempt.history"
    ),
    BottomNavigationItem(
        label = "Profile",
        route = Screen.Profile.route,
        icon = Icons.Default.Person,
        relatedRoutes = setOf(
            Screen.Profile.route,
            Screen.EditProfile.route
        )
    )
)

@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    currentRoute: String?,
    hasPermission: (String) -> Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .height(74.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        bottomNavigationItems
            .filter { item ->
                item.requiredPermission == null || hasPermission(item.requiredPermission)
            }
            .forEach { item ->
            val isSelected = currentRoute in item.relatedRoutes
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(74.dp)
                    .clickable {
                        if (!isSelected) {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(2.dp)
                        .background(if (isSelected) Color(0xFF4968A8) else Color.Transparent)
                )
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.padding(top = 13.dp).size(23.dp),
                    tint = if (isSelected) Color(0xFF4968A8) else Color(0xFF71767F)
                )
                Text(
                    text = item.label,
                    modifier = Modifier.padding(top = 5.dp),
                    color = if (isSelected) Color(0xFF4968A8) else Color(0xFF484C52),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}
