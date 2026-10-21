package com.englishlearningapp.ui.screens.quiz

/**
 * Represents the result of a single question in the review.
 */
data class ReviewItem(
    val questionId: Int,
    val questionText: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean
)

/**
 * Holds the complete result of a finished quiz.
 */
data class QuizResult(
    val testName: String,
    val score: Int,
    val total: Int,
    val reviewItems: List<ReviewItem>,
    val startedTime: String? = null,
    val finishedTime: String? = null,
    val totalTime: String? = null
)

/**
 * Simple singleton to pass quiz results from QuizTakingScreen to ResultScreen.
 * This avoids the complexity of serializing result data into navigation arguments.
 */
object QuizResultHolder {
    var result: QuizResult? = null
}
