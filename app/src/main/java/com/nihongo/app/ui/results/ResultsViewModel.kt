package com.nihongo.app.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nihongo.app.data.database.NihongoDatabase
import com.nihongo.app.data.database.entity.TestWithQuestions
import kotlinx.coroutines.flow.*

class ResultsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NihongoDatabase.getDatabase(application)
    private val testDao = database.testDao()

    val completedTests: StateFlow<List<TestWithQuestions>> = testDao.getAllTests()
        .map { list -> list.filter { it.test.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTestsTaken: StateFlow<Int> = completedTests
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageScorePercentage: StateFlow<Float> = completedTests
        .map { list ->
            if (list.isEmpty()) 0f else {
                val totalPercentage = list.sumOf { 
                    if (it.test.totalQuestions > 0) (it.test.score.toDouble() / it.test.totalQuestions) * 100.0 else 0.0 
                }
                (totalPercentage / list.size).toFloat()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
}
