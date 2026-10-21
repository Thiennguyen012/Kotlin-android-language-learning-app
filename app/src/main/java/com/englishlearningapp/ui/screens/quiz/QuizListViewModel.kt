package com.englishlearningapp.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.englishlearningapp.core.network.toBackendMessage
import com.englishlearningapp.data.remote.dto.quiz.CollectionTestDto
import com.englishlearningapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the quiz list screen.
 */
data class QuizListUiState(
    val tests: List<CollectionTestDto> = emptyList(),
    val selectedFilter: QuizFilter = QuizFilter.NEWEST,
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val totalTests: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class QuizFilter {
    NEWEST,
    SHORTEST_DURATION,
    FEWEST_QUESTIONS,
    NAME_ASCENDING
}

/**
 * ViewModel for displaying the list of available quizzes/tests.
 */
class QuizListViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private var currentPageTests: List<CollectionTestDto> = emptyList()

    private val _uiState = MutableStateFlow(QuizListUiState())
    val uiState: StateFlow<QuizListUiState> = _uiState.asStateFlow()

    init {
        loadPage(1)
    }

    fun loadTests() {
        loadPage(_uiState.value.currentPage)
    }

    fun nextPage() {
        val state = _uiState.value
        if (!state.isLoading && state.currentPage < state.lastPage) {
            loadPage(state.currentPage + 1)
        }
    }

    fun previousPage() {
        val state = _uiState.value
        if (!state.isLoading && state.currentPage > 1) {
            loadPage(state.currentPage - 1)
        }
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val (tests, meta) = quizRepository.getTests(page = page)
                currentPageTests = tests
                _uiState.value = _uiState.value.copy(
                    tests = filterTests(tests, _uiState.value.selectedFilter),
                    currentPage = meta?.currentPage ?: page,
                    lastPage = meta?.lastPage ?: 1,
                    totalTests = meta?.total ?: tests.size,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toBackendMessage()
                )
            }
        }
    }

    fun selectFilter(filter: QuizFilter) {
        _uiState.value = _uiState.value.copy(
            tests = filterTests(currentPageTests, filter),
            selectedFilter = filter
        )
    }

    private fun filterTests(
        tests: List<CollectionTestDto>,
        filter: QuizFilter
    ): List<CollectionTestDto> {
        return when (filter) {
            QuizFilter.NEWEST -> tests.sortedByDescending { it.updatedAt }
            QuizFilter.SHORTEST_DURATION -> tests.sortedBy { it.duration }
            QuizFilter.FEWEST_QUESTIONS -> tests.sortedBy { it.totalQuestions }
            QuizFilter.NAME_ASCENDING -> tests.sortedBy { it.testName.lowercase() }
        }
    }

    /**
     * Factory for creating this ViewModel with dependencies.
     */
    class Factory(
        private val quizRepository: QuizRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuizListViewModel::class.java)) {
                return QuizListViewModel(quizRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
