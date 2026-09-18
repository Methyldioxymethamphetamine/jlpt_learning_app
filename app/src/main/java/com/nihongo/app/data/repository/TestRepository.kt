package com.nihongo.app.data.repository

import android.content.Context
import com.nihongo.app.ai.GeminiClient
import com.nihongo.app.ai.TestGenerationConfig
import com.nihongo.app.data.database.dao.TestDao
import com.nihongo.app.data.database.dao.VocabularyDao
import com.nihongo.app.data.database.entity.QuestionOptionEntity
import com.nihongo.app.data.database.entity.TestEntity
import com.nihongo.app.data.database.entity.TestQuestionEntity
import com.nihongo.app.data.database.entity.TestWithQuestions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TestRepository(private val testDao: TestDao, private val context: Context) {
    val allTests: Flow<List<TestWithQuestions>> = testDao.getAllTests()
    private val geminiClient = GeminiClient(context)

    suspend fun getTestWithQuestions(testId: Long): TestWithQuestions? {
        return testDao.getTestWithQuestions(testId)
    }

    suspend fun updateTestScore(testId: Long, score: Int, isCompleted: Boolean) {
        val testWithQuestions = testDao.getTestWithQuestions(testId)
        testWithQuestions?.test?.let { test ->
            val updated = test.copy(score = score, isCompleted = isCompleted)
            testDao.updateTest(updated)
        }
    }

    suspend fun saveCompletedTestResults(testId: Long, userAnswersMap: Map<Long, String>, score: Int) {
        val testWithQuestions = testDao.getTestWithQuestions(testId) ?: return

        for (qWithOptions in testWithQuestions.questions) {
            val q = qWithOptions.question
            val answer = userAnswersMap[q.id]
            if (answer != null) {
                val updatedQ = q.copy(userAnswer = answer)
                testDao.updateQuestion(updatedQ)
            }
        }

        val updatedTest = testWithQuestions.test.copy(
            score = score,
            isCompleted = true
        )
        testDao.updateTest(updatedTest)
    }

    suspend fun generateAiTest(config: TestGenerationConfig, vocabularyDao: VocabularyDao) {
        val vocabWithStateList = vocabularyDao.getVocabularyWithStateByLevel(config.jlptLevel).first()
            .ifEmpty { vocabularyDao.getAllVocabularyWithState().first() }

        val learnedVocabList = vocabWithStateList.map { it.vocabulary }

        val payload = geminiClient.generateTest(config, learnedVocabList, vocabWithStateList) ?: return

        val testId = testDao.insertTest(
            TestEntity(
                jlptLevel = payload.jlptLevel,
                configSummary = payload.configSummary,
                totalQuestions = payload.questions.size
            )
        )

        for (q in payload.questions) {
            val qId = testDao.insertQuestion(
                TestQuestionEntity(
                    testId = testId,
                    questionType = q.questionType,
                    questionText = q.questionText,
                    listeningText = q.listeningText,
                    correctAnswer = q.correctAnswer,
                    explanation = q.explanation,
                    targetVocabulary = q.targetVocabulary
                )
            )
            val optionEntities = q.options.map { QuestionOptionEntity(questionId = qId, optionText = it) }
            testDao.insertOptions(optionEntities)
        }
    }
}
