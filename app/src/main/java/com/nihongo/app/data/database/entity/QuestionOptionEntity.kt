package com.nihongo.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_options",
    foreignKeys = [
        ForeignKey(
            entity = TestQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["questionId"])]
)
data class QuestionOptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val questionId: Long,
    val optionText: String
)
