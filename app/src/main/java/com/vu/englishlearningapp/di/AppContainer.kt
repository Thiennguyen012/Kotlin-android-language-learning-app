package com.vu.englishlearningapp.di

import android.content.Context
import com.vu.englishlearningapp.core.network.RetrofitClient
import com.vu.englishlearningapp.core.session.TokenManager
import com.vu.englishlearningapp.data.remote.api.AuthApi
import com.vu.englishlearningapp.data.remote.api.CollectionApi
import com.vu.englishlearningapp.data.remote.api.FlashcardApi
import com.vu.englishlearningapp.data.remote.api.ProfileApi
import com.vu.englishlearningapp.data.remote.api.QuizApi
import com.vu.englishlearningapp.data.remote.api.QuestionApi
import com.vu.englishlearningapp.data.remote.api.PermissionApi
import com.vu.englishlearningapp.data.remote.api.RoleApi
import com.vu.englishlearningapp.data.remote.api.UserApi
import com.vu.englishlearningapp.data.remote.api.SpeakingAssessmentApi
import com.vu.englishlearningapp.data.remote.api.SpeakingLevelAssessmentApi
import com.vu.englishlearningapp.data.repository.AuthRepository
import com.vu.englishlearningapp.data.repository.CollectionRepository
import com.vu.englishlearningapp.data.repository.FlashcardRepository
import com.vu.englishlearningapp.data.repository.ProfileRepository
import com.vu.englishlearningapp.data.repository.QuizRepository
import com.vu.englishlearningapp.data.repository.QuestionRepository
import com.vu.englishlearningapp.data.repository.PermissionRepository
import com.vu.englishlearningapp.data.repository.RoleRepository
import com.vu.englishlearningapp.data.repository.UserRepository
import com.vu.englishlearningapp.data.repository.SpeakingAssessmentRepository
import com.vu.englishlearningapp.data.repository.SpeakingLevelAssessmentRepository

/**
 * Manual dependency injection container.
 * Creates and holds all shared dependencies for the app.
 *
 * This is a simple alternative to Hilt/Dagger for beginners.
 * All dependencies are created lazily (only when first accessed).
 */
class AppContainer(context: Context) {

    // --- Core ---
    val tokenManager = TokenManager(context)

    // --- Network ---
    private val retrofit = RetrofitClient.create(tokenManager)

    // --- API Services ---
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val flashcardApi: FlashcardApi = retrofit.create(FlashcardApi::class.java)
    val quizApi: QuizApi = retrofit.create(QuizApi::class.java)
    val questionApi: QuestionApi = retrofit.create(QuestionApi::class.java)
    val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)
    val collectionApi: CollectionApi = retrofit.create(CollectionApi::class.java)
    val permissionApi: PermissionApi = retrofit.create(PermissionApi::class.java)
    val roleApi: RoleApi = retrofit.create(RoleApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val speakingAssessmentApi: SpeakingAssessmentApi = retrofit.create(SpeakingAssessmentApi::class.java)
    val speakingLevelAssessmentApi: SpeakingLevelAssessmentApi = retrofit.create(SpeakingLevelAssessmentApi::class.java)

    // --- Repositories ---
    val authRepository = AuthRepository(authApi, tokenManager)
    val flashcardRepository = FlashcardRepository(flashcardApi)
    val quizRepository = QuizRepository(quizApi)
    val questionRepository = QuestionRepository(questionApi)
    val profileRepository = ProfileRepository(authApi, profileApi)
    val collectionRepository = CollectionRepository(collectionApi)
    val permissionRepository = PermissionRepository(permissionApi)
    val roleRepository = RoleRepository(roleApi)
    val userRepository = UserRepository(userApi)
    val speakingAssessmentRepository = SpeakingAssessmentRepository(speakingAssessmentApi)
    val speakingLevelAssessmentRepository = SpeakingLevelAssessmentRepository(speakingLevelAssessmentApi)
}
