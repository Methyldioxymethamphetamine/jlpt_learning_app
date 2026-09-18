package com.nihongo.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_questions",
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["testId"])]
)
data class TestQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val testId: Long,
    val questionType: String,
    val questionText: String,
    val listeningText: String? = null,
    val correctAnswer: String,
    val userAnswer: String? = null,
    val explanation: String,
    val targetVocabulary: String? = null
)
