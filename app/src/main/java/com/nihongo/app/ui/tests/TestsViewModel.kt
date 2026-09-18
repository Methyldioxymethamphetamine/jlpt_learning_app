package com.nihongo.app.ui.tests

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nihongo.app.ai.AiExplainer
import com.nihongo.app.ai.TestGenerationConfig
import com.nihongo.app.data.database.NihongoDatabase
import com.nihongo.app.data.database.entity.TestQuestionEntity
import com.nihongo.app.data.database.entity.TestWithQuestions
import com.nihongo.app.data.repository.TestRepository
import com.nihongo.app.tts.TtsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TestsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NihongoDatabase.getDatabase(application)
    private val repository = TestRepository(database.testDao(), application)
    private val ttsManager = TtsManager(application)
    private val aiExplainer = AiExplainer(application)
    private val prefs = application.getSharedPreferences("nihongo_prefs", Context.MODE_PRIVATE)

    val allTests: StateFlow<List<TestWithQuestions>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun generateTest(config: TestGenerationConfig) {
        viewModelScope.launch {
            repository.generateAiTest(config, database.vocabularyDao())
        }
    }

    fun playAudio(text: String) {
        val speed = prefs.getFloat("speech_rate", 1.0f)
        ttsManager.speak(text, speed)
    }

    suspend fun getTest(testId: Long): TestWithQuestions? {
        return repository.getTestWithQuestions(testId)
    }

    fun saveDetailedTestResults(testId: Long, userAnswersMap: Map<Long, String>, score: Int) {
        viewModelScope.launch {
            repository.saveCompletedTestResults(testId, userAnswersMap, score)
        }
    }

    suspend fun explainQuestionWithAi(question: TestQuestionEntity, userAnswer: String?, correctAnswer: String): String {
        return aiExplainer.explainQuestion(question, userAnswer, correctAnswer)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
