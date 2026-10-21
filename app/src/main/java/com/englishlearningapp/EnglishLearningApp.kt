package com.englishlearningapp

import android.app.Application
import com.englishlearningapp.di.AppContainer

/**
 * Custom Application class that initializes the dependency injection container.
 * Referenced in AndroidManifest.xml via android:name=".EnglishLearningApp"
 */
class EnglishLearningApp : Application() {

    // The DI container is available app-wide
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
