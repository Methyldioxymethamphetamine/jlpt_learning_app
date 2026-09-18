package com.nihongo.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val japanese: String,
    val reading: String,
    val romaji: String,
    val type: String,
    val meaning: String,
    val jlptLevel: String
)
