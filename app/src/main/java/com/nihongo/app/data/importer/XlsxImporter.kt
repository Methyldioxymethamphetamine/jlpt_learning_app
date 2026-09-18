package com.nihongo.app.data.importer

import android.content.Context
import com.nihongo.app.data.database.entity.VocabularyEntity
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

class XlsxImporter(private val context: Context) {

    fun importVocabulary(inputStream: InputStream): List<VocabularyEntity> {
        val vocabularyList = mutableListOf<VocabularyEntity>()
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        for (rowIndex in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            if (rowIndex == 0) {
                val firstCellText = getCellText(row, 0).lowercase()
                if (firstCellText.contains("japanese") || firstCellText.contains("kanji") || firstCellText.contains("word")) {
                    continue
                }
            }

            val japanese = getCellText(row, 0)
            if (japanese.isBlank()) continue

            val reading = getCellText(row, 1).ifBlank { japanese }
            val romaji = getCellText(row, 2)
            val type = getCellText(row, 3).ifBlank { "noun" }
            val meaning = getCellText(row, 4)
            val jlptLevel = getCellText(row, 5).uppercase().ifBlank { "N5" }

            vocabularyList.add(
                VocabularyEntity(
                    japanese = japanese.trim(),
                    reading = reading.trim(),
                    romaji = romaji.trim(),
                    type = type.trim(),
                    meaning = meaning.trim(),
                    jlptLevel = jlptLevel.trim()
                )
            )
        }
        workbook.close()
        return vocabularyList
    }

    private fun getCellText(row: org.apache.poi.ss.usermodel.Row, cellIndex: Int): String {
        val cell = row.getCell(cellIndex) ?: return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString().removeSuffix(".0")
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue
                } catch (e: Exception) {
                    try {
                        cell.numericCellValue.toString().removeSuffix(".0")
                    } catch (e2: Exception) {
                        ""
                    }
                }
            }
            else -> ""
        }
    }
}
