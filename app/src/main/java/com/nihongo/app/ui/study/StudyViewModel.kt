package com.nihongo.app.ui.study

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nihongo.app.data.database.NihongoDatabase
import com.nihongo.app.data.database.entity.LearningStateEntity
import com.nihongo.app.data.database.entity.VocabularyWithState
import com.nihongo.app.tts.TtsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DeckOrdering(val displayName: String) {
    ASCENDING("Database Ascending"),
    LEAST_KNOWN_FIRST("Least Known → Most Known"),
    MOST_KNOWN_FIRST("Most Known → Least Known"),
    RANDOM("Random"),
    REVIEW_DUE("Review / Due Priority")
}

data class StudyCard(
    val item: VocabularyWithState,
    var sessionAttempts: Int = 0,
    var consecutiveKnown: Int = 0
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NihongoDatabase.getDatabase(application)
    private val vocabularyDao = database.vocabularyDao()
    private val learningStateDao = database.learningStateDao()
    private val prefs = application.getSharedPreferences("nihongo_prefs", Context.MODE_PRIVATE)
    private val ttsManager = TtsManager(application)

    private val _selectedLevel = MutableStateFlow("N5")
    val selectedLevel: StateFlow<String> = _selectedLevel

    private val _selectedOrdering = MutableStateFlow(DeckOrdering.LEAST_KNOWN_FIRST)
    val selectedOrdering: StateFlow<DeckOrdering> = _selectedOrdering

    private val _isConfiguring = MutableStateFlow(true)
    val isConfiguring: StateFlow<Boolean> = _isConfiguring

    private val _deck = MutableStateFlow<List<StudyCard>>(emptyList())
    val deck: StateFlow<List<StudyCard>> = _deck

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isRevealed = MutableStateFlow(false)
    val isRevealed: StateFlow<Boolean> = _isRevealed

    private val _sessionComplete = MutableStateFlow(false)
    val sessionComplete: StateFlow<Boolean> = _sessionComplete

    fun setLevel(level: String) {
        _selectedLevel.value = level
    }

    fun setOrdering(ordering: DeckOrdering) {
        _selectedOrdering.value = ordering
    }

    fun startSession() {
        viewModelScope.launch {
            val level = _selectedLevel.value
            val ordering = _selectedOrdering.value

            vocabularyDao.getVocabularyWithStateByLevel(level).first().let { rawList ->
                if (rawList.isEmpty()) {
                    _deck.value = emptyList()
                    _isConfiguring.value = false
                    return@let
                }

                val sortedList = when (ordering) {
                    DeckOrdering.ASCENDING -> rawList.sortedBy { it.vocabulary.id }
                    DeckOrdering.LEAST_KNOWN_FIRST -> rawList.sortedBy { it.learningState?.masteryScore ?: 0f }
                    DeckOrdering.MOST_KNOWN_FIRST -> rawList.sortedByDescending { it.learningState?.masteryScore ?: 0f }
                    DeckOrdering.RANDOM -> rawList.shuffled()
                    DeckOrdering.REVIEW_DUE -> rawList.sortedBy { it.learningState?.nextReview ?: 0L }
                }

                _deck.value = sortedList.map { StudyCard(item = it) }
                _currentIndex.value = 0
                _isRevealed.value = false
                _sessionComplete.value = false
                _isConfiguring.value = false
            }
        }
    }

    fun toggleReveal() {
        _isRevealed.value = !_isRevealed.value
    }

    fun pronounceWord(text: String) {
        val speed = prefs.getFloat("speech_rate", 1.0f)
        ttsManager.speak(text, speed)
    }

    fun previousCard() {
        if (_currentIndex.value > 0) {
            _currentIndex.value = _currentIndex.value - 1
            _isRevealed.value = false
            _sessionComplete.value = false
        }
    }

    fun answerCard(known: Boolean) {
        val currentDeck = _deck.value
        val index = _currentIndex.value
        if (index >= currentDeck.size) return

        val studyCard = currentDeck[index]
        studyCard.sessionAttempts++

        viewModelScope.launch {
            val vocabId = studyCard.item.vocabulary.id
            val currentState = studyCard.item.learningState ?: LearningStateEntity(vocabularyId = vocabId)

            val newTimesSeen = currentState.timesSeen + 1
            val newKnownCount = if (known) currentState.knownCount + 1 else currentState.knownCount
            val newUnknownCount = if (!known) currentState.unknownCount + 1 else currentState.unknownCount
            
            val baseScore = if (known) 0.2f else -0.15f
            val newMastery = (currentState.masteryScore + baseScore).coerceIn(0f, 1f)
            val newStreak = if (known) currentState.streak + 1 else 0
            val newStateStr = when {
                newMastery >= 0.8f -> "MASTERED"
                newMastery >= 0.5f -> "REVIEW"
                newMastery > 0.1f -> "LEARNING"
                else -> "NEW"
            }

            val updatedState = currentState.copy(
                timesSeen = newTimesSeen,
                knownCount = newKnownCount,
                unknownCount = newUnknownCount,
                masteryScore = newMastery,
                lastSeen = System.currentTimeMillis(),
                streak = newStreak,
                learningState = newStateStr
            )

            learningStateDao.updateLearningState(updatedState)

            val mutableDeck = currentDeck.toMutableList()
            if (!known && mutableDeck.size > 1) {
                mutableDeck.removeAt(index)
                val reinsertIndex = (index + 3).coerceAtMost(mutableDeck.size)
                mutableDeck.add(reinsertIndex, studyCard)
                _deck.value = mutableDeck
            } else {
                if (index + 1 >= mutableDeck.size) {
                    _sessionComplete.value = true
                } else {
                    _currentIndex.value = index + 1
                    _isRevealed.value = false
                }
            }
        }
    }

    fun resetSessionConfig() {
        _isConfiguring.value = true
        _sessionComplete.value = false
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
