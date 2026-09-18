package com.nihongo.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nihongo.app.data.database.entity.QuestionOptionEntity
import com.nihongo.app.data.database.entity.TestEntity
import com.nihongo.app.data.database.entity.TestQuestionEntity
import com.nihongo.app.data.database.entity.TestWithQuestions
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: TestQuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<QuestionOptionEntity>)

    @Transaction
    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<TestWithQuestions>>

    @Transaction
    @Query("SELECT * FROM tests WHERE id = :testId")
    suspend fun getTestWithQuestions(testId: Long): TestWithQuestions?

    @Update
    suspend fun updateTest(test: TestEntity)

    @Update
    suspend fun updateQuestion(question: TestQuestionEntity)
}
