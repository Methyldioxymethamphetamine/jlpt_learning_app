package com.nihongo.app.data.repository

import com.nihongo.app.data.database.dao.LearningStateDao
import com.nihongo.app.data.database.dao.VocabularyDao
import com.nihongo.app.data.database.entity.LearningStateEntity
import com.nihongo.app.data.database.entity.VocabularyEntity
import com.nihongo.app.data.database.entity.VocabularyWithState
import com.nihongo.app.data.importer.XlsxImporter
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

class VocabularyRepository(
    private val vocabularyDao: VocabularyDao,
    private val learningStateDao: LearningStateDao,
    private val xlsxImporter: XlsxImporter
) {
    val allVocabulary: Flow<List<VocabularyEntity>> = vocabularyDao.getAllVocabulary()
    val allVocabularyWithState: Flow<List<VocabularyWithState>> = vocabularyDao.getAllVocabularyWithState()

    fun getVocabularyByLevel(level: String): Flow<List<VocabularyWithState>> {
        return vocabularyDao.getVocabularyWithStateByLevel(level)
    }

    suspend fun importFromStream(inputStream: InputStream) {
        val list = xlsxImporter.importVocabulary(inputStream)
        for (vocab in list) {
            val id = vocabularyDao.insertVocabulary(vocab)
            val existingState = learningStateDao.getLearningStateForWord(id)
            if (existingState == null) {
                learningStateDao.insertLearningState(
                    LearningStateEntity(vocabularyId = id)
                )
            }
        }
    }
}
