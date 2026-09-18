package com.nihongo.app.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class VocabularyWithState(
    @Embedded
    val vocabulary: VocabularyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vocabularyId"
    )
    val learningState: LearningStateEntity?
)
