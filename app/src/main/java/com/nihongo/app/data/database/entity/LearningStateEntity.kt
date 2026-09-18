package com.nihongo.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_state",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vocabularyId"], unique = true)]
)
data class LearningStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vocabularyId: Long,
    val timesSeen: Int = 0,
    val knownCount: Int = 0,
    val unknownCount: Int = 0,
    val masteryScore: Float = 0f,
    val lastSeen: Long = 0L,
    val nextReview: Long = 0L,
    val streak: Int = 0,
    val difficulty: Float = 2.5f,
    val starred: Boolean = false,
    val learningState: String = "NEW"
)
