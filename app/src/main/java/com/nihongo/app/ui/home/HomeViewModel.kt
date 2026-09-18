package com.nihongo.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nihongo.app.data.database.NihongoDatabase
import com.nihongo.app.data.database.entity.VocabularyWithState
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class HomeStats(
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val learnedToday: Int = 0,
    val n5Learned: Int = 0,
    val n5Total: Int = 0,
    val n4Learned: Int = 0,
    val n4Total: Int = 0,
    val n3Learned: Int = 0,
    val n3Total: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NihongoDatabase.getDatabase(application)
    private val vocabularyDao = database.vocabularyDao()

    val stats: StateFlow<HomeStats> = vocabularyDao.getAllVocabularyWithState()
        .map { list -> calculateStats(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeStats())

    private fun calculateStats(list: List<VocabularyWithState>): HomeStats {
        if (list.isEmpty()) return HomeStats()

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val totalWords = list.size
        val learnedList = list.filter { (it.learningState?.masteryScore ?: 0f) > 0.1f || (it.learningState?.knownCount ?: 0) > 0 }
        val learnedWords = learnedList.size

        val learnedToday = list.count { (it.learningState?.lastSeen ?: 0L) >= startOfToday }

        val n5List = list.filter { it.vocabulary.jlptLevel.equals("N5", ignoreCase = true) }
        val n5Learned = n5List.count { (it.learningState?.masteryScore ?: 0f) > 0.1f || (it.learningState?.knownCount ?: 0) > 0 }

        val n4List = list.filter { it.vocabulary.jlptLevel.equals("N4", ignoreCase = true) }
        val n4Learned = n4List.count { (it.learningState?.masteryScore ?: 0f) > 0.1f || (it.learningState?.knownCount ?: 0) > 0 }

        val n3List = list.filter { it.vocabulary.jlptLevel.equals("N3", ignoreCase = true) }
        val n3Learned = n3List.count { (it.learningState?.masteryScore ?: 0f) > 0.1f || (it.learningState?.knownCount ?: 0) > 0 }

        return HomeStats(
            totalWords = totalWords,
            learnedWords = learnedWords,
            learnedToday = learnedToday,
            n5Learned = n5Learned,
            n5Total = n5List.size,
            n4Learned = n4Learned,
            n4Total = n4List.size,
            n3Learned = n3Learned,
            n3Total = n3List.size
        )
    }
}
