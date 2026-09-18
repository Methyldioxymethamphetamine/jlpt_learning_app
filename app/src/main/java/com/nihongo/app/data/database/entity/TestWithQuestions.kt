package com.nihongo.app.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class QuestionWithOptions(
    @Embedded
    val question: TestQuestionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val options: List<QuestionOptionEntity>
)

data class TestWithQuestions(
    @Embedded
    val test: TestEntity,
    @Relation(
        entity = TestQuestionEntity::class,
        parentColumn = "id",
        entityColumn = "testId"
    )
    val questions: List<QuestionWithOptions>
)
