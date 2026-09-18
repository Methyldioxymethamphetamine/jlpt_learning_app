package com.nihongo.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val jlptLevel: String,
    val configSummary: String,
    val createdAt: Long = System.currentTimeMillis(),
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val isCompleted: Boolean = false
)
