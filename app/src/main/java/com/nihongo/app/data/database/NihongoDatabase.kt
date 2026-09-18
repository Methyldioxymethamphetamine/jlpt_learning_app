package com.nihongo.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nihongo.app.data.database.dao.LearningStateDao
import com.nihongo.app.data.database.dao.TestDao
import com.nihongo.app.data.database.dao.VocabularyDao
import com.nihongo.app.data.database.entity.LearningStateEntity
import com.nihongo.app.data.database.entity.QuestionOptionEntity
import com.nihongo.app.data.database.entity.TestEntity
import com.nihongo.app.data.database.entity.TestQuestionEntity
import com.nihongo.app.data.database.entity.VocabularyEntity

@Database(
    entities = [
        VocabularyEntity::class,
        LearningStateEntity::class,
        TestEntity::class,
        TestQuestionEntity::class,
        QuestionOptionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NihongoDatabase : RoomDatabase() {
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun learningStateDao(): LearningStateDao
    abstract fun testDao(): TestDao

    companion object {
        @Volatile
        private var INSTANCE: NihongoDatabase? = null

        fun getDatabase(context: Context): NihongoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NihongoDatabase::class.java,
                    "nihongo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
