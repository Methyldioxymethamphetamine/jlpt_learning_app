package com.nihongo.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nihongo.app.data.database.entity.LearningStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningStateDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLearningState(state: LearningStateEntity)

    @Update
    suspend fun updateLearningState(state: LearningStateEntity)

    @Query("SELECT * FROM learning_state WHERE vocabularyId = :vocabularyId")
    suspend fun getLearningStateForWord(vocabularyId: Long): LearningStateEntity?

    @Query("SELECT * FROM learning_state")
    fun getAllLearningStates(): Flow<List<LearningStateEntity>>
}
