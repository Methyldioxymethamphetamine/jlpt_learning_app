package com.nihongo.app.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nihongo.app.data.database.NihongoDatabase
import com.nihongo.app.data.database.entity.LearningStateEntity
import com.nihongo.app.data.database.entity.VocabularyWithState
import com.nihongo.app.data.importer.CsvImporter
import com.nihongo.app.data.importer.XlsxImporter
import com.nihongo.app.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NihongoDatabase.getDatabase(application)
    private val repository = VocabularyRepository(
        database.vocabularyDao(),
        database.learningStateDao(),
        XlsxImporter(application)
    )
    private val csvImporter = CsvImporter(application)

    private val _selectedLevel = MutableStateFlow<String?>("N5")
    val selectedLevel: StateFlow<String?> = _selectedLevel

    val vocabularyList: StateFlow<List<VocabularyWithState>> = _selectedLevel
        .flatMapLatest { level ->
            if (level != null) {
                repository.getVocabularyByLevel(level)
            } else {
                repository.allVocabularyWithState
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.allVocabulary.collect { list ->
                if (list.isEmpty()) {
                    loadMasterCsvData()
                }
            }
        }
    }

    fun selectLevel(level: String?) {
        _selectedLevel.value = level
    }

    fun importXlsx(inputStream: InputStream) {
        viewModelScope.launch {
            repository.importFromStream(inputStream)
        }
    }

    private suspend fun loadMasterCsvData() {
        val app = getApplication<Application>()
        val assetManager = app.assets

        val levels = listOf("N5" to "N5Vocab.csv", "N4" to "N4Vocab.csv", "N3" to "N3Vocab.csv")
        for ((level, fileName) in levels) {
            try {
                val inputStream = assetManager.open(fileName)
                val vocabList = csvImporter.importCsvFromStream(inputStream, level)
                for (vocab in vocabList) {
                    val id = database.vocabularyDao().insertVocabulary(vocab)
                    val existingState = database.learningStateDao().getLearningStateForWord(id)
                    if (existingState == null) {
                        database.learningStateDao().insertLearningState(
                            LearningStateEntity(vocabularyId = id)
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore missing file errors
            }
        }
    }
}
