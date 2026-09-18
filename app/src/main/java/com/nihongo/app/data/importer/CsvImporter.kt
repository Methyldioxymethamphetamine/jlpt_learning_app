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

        if (lines.isEmpty()) return list

        val startIndex = if (lines[0].contains("Kanji", ignoreCase = true) || lines[0].contains("Japanese", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val cols = parseCsvLine(line)
            if (cols.isEmpty()) continue

            val kanji = cols.getOrNull(0)?.trim() ?: ""
            val kana = cols.getOrNull(1)?.trim() ?: ""
            val romaji = cols.getOrNull(2)?.trim() ?: ""
            val type = cols.getOrNull(3)?.trim() ?: ""
            val meaning = cols.getOrNull(4)?.trim() ?: ""

            if (kanji.isBlank() && kana.isBlank()) continue

            list.add(
                VocabularyEntity(
                    japanese = if (kanji.isNotBlank()) kanji else kana,
                    reading = if (kana.isNotBlank()) kana else kanji,
                    romaji = romaji,
                    type = if (type.isNotBlank()) type else "Noun",
                    meaning = meaning,
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
