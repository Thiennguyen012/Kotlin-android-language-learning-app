package com.englishlearningapp.ui.navigation

/**
 * Defines all navigation routes in the app.
 * Using a sealed class keeps routes type-safe and in one place.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object SpeakingPractice : Screen("speaking_practice")
    data object SpeakingLevelAssessment : Screen("speaking_level_assessment")

    // Flashcard flow
    data object FlashcardCollections : Screen("flashcard_collections")
    data object FlashcardStudy : Screen("flashcard_study/{collectionId}") {
        fun createRoute(collectionId: Int) = "flashcard_study/$collectionId"
    }

    // Quiz flow
    data object QuizList : Screen("quiz_list")
    data object QuizDetail : Screen("quiz_detail/{testId}") {
        fun createRoute(testId: Int) = "quiz_detail/$testId"
    }
    data object QuizTaking : Screen("quiz_taking/{testId}") {
        fun createRoute(testId: Int) = "quiz_taking/$testId"
    }
    data object QuizResult : Screen("quiz_result")
    data object AttemptHistory : Screen("attempt_history")
    data object AttemptHistoryDetail : Screen("attempt_history/{attemptId}") {
        fun createRoute(attemptId: Int) = "attempt_history/$attemptId"
    }

    // Profile flow
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")

    // Admin Collection Management flow
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminUserManagement : Screen("admin_user_management")
    data object AdminUserDetail : Screen("admin_users/{userId}") {
        fun createRoute(userId: Int) = "admin_users/$userId"
    }
    data object AdminUserCreate : Screen("admin_users/create")
    data object AdminUserEdit : Screen("admin_users/edit/{userId}") {
        fun createRoute(userId: Int) = "admin_users/edit/$userId"
    }
    data object AdminPermissionList : Screen("admin_permission_list")
    data object AdminPermissionCreate : Screen("admin_permissions/create")
    data object AdminPermissionEdit : Screen("admin_permissions/edit/{permissionId}") {
        fun createRoute(permissionId: Int) = "admin_permissions/edit/$permissionId"
    }
    data object AdminRoleList : Screen("admin_role_list")
    data object AdminRoleCreate : Screen("admin_roles/create")
    data object AdminRoleEdit : Screen("admin_roles/edit/{roleId}") {
        fun createRoute(roleId: Int) = "admin_roles/edit/$roleId"
    }
    data object AdminCollectionList : Screen("admin_collection_list")
    data object AdminCollectionDetail : Screen("admin_collection_detail/{id}") {
        fun createRoute(id: Int) = "admin_collection_detail/$id"
    }
    data object AdminCreateCollection : Screen("admin_create_collection")
    data object AdminEditCollection : Screen("admin_edit_collection/{id}") {
        fun createRoute(id: Int) = "admin_edit_collection/$id"
    }
    data object AdminFlashcardList : Screen("admin_flashcard_list")
    data object AdminStandaloneFlashcardCreate : Screen("admin_flashcards/create")
    data object AdminStandaloneFlashcardEdit : Screen("admin_flashcards/edit/{flashcardId}") {
        fun createRoute(flashcardId: Int) = "admin_flashcards/edit/$flashcardId"
    }
    data object AdminQuestionList : Screen("admin_question_list")
    data object AdminQuestionCreate : Screen("admin_questions/create")
    data object AdminQuestionEdit : Screen("admin_questions/edit/{questionId}") {
        fun createRoute(questionId: Int) = "admin_questions/edit/$questionId"
    }
    data object AdminTestList : Screen("admin_test_list")
    data object AdminTestCreate : Screen("admin_tests/create")
    data object AdminTestEdit : Screen("admin_tests/edit/{testId}") {
        fun createRoute(testId: Int) = "admin_tests/edit/$testId"
    }
    data object AdminFlashcardCreate : Screen("admin_flashcard_create/{collectionId}") {
        fun createRoute(collectionId: Int) = "admin_flashcard_create/$collectionId"
    }
    data object AdminFlashcardEdit : Screen("admin_flashcard_edit/{collectionId}/{flashcardId}") {
        fun createRoute(collectionId: Int, flashcardId: Int) = "admin_flashcard_edit/$collectionId/$flashcardId"
    }
}
