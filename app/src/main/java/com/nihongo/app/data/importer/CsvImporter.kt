package com.nihongo.app.data.importer

import android.content.Context
import com.nihongo.app.data.database.entity.VocabularyEntity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class CsvImporter(private val context: Context) {

    fun importCsvFromStream(inputStream: InputStream, jlptLevel: String): List<VocabularyEntity> {
        val list = mutableListOf<VocabularyEntity>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines()
        reader.close()

        var currentJapanese = ""
        var currentRomaji = ""
        var currentType = ""
        var currentMeaning = ""
        var currentReading = ""

        for (i in 1 until lines.size) {
            val line = lines[i]
            val cols = parseCsvLine(line)
            if (cols.isEmpty()) continue

            val col0 = cols.getOrNull(0)?.trim() ?: ""
            val col1 = cols.getOrNull(1)?.trim() ?: ""
            val col2 = cols.getOrNull(2)?.trim() ?: ""
            val col3 = cols.getOrNull(3)?.trim() ?: ""
            val col4 = cols.getOrNull(4)?.trim() ?: ""

            if (col0.toIntOrNull() != null) {
                if (currentJapanese.isNotBlank()) {
                    list.add(
                        VocabularyEntity(
                            japanese = currentJapanese,
                            reading = currentReading.ifBlank { currentJapanese },
                            romaji = currentRomaji,
                            type = currentType.ifBlank { "noun" },
                            meaning = currentMeaning,
                            jlptLevel = jlptLevel
                        )
                    )
                }
                currentJapanese = col1
                currentRomaji = col2
                currentType = col3
                currentMeaning = col4
                currentReading = ""
            } else {
                if (col2.isNotBlank() && currentReading.isBlank()) {
                    currentReading = col2
                }
            }
        }

        if (currentJapanese.isNotBlank()) {
            list.add(
                VocabularyEntity(
                    japanese = currentJapanese,
                    reading = currentReading.ifBlank { currentJapanese },
                    romaji = currentRomaji,
                    type = currentType.ifBlank { "noun" },
                    meaning = currentMeaning,
                    jlptLevel = jlptLevel
                )
            )
        }

        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        result.add(sb.toString())
        return result
    }
}
