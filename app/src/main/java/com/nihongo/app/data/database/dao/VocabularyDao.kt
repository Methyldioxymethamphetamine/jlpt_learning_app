package com.nihongo.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nihongo.app.data.database.entity.VocabularyEntity
import com.nihongo.app.data.database.entity.VocabularyWithState
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabularyList(vocabularyList: List<VocabularyEntity>)

    @Query("SELECT * FROM vocabulary")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE jlptLevel = :level")
    fun getVocabularyByLevel(level: String): Flow<List<VocabularyEntity>>

    @Transaction
    @Query("SELECT * FROM vocabulary")
    fun getAllVocabularyWithState(): Flow<List<VocabularyWithState>>

    @Transaction
    @Query("SELECT * FROM vocabulary WHERE jlptLevel = :level")
    fun getVocabularyWithStateByLevel(level: String): Flow<List<VocabularyWithState>>

    @Query("DELETE FROM vocabulary")
    suspend fun deleteAllVocabulary()
}
