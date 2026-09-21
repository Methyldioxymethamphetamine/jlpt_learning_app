package com.nihongo.app.data.importer

import android.content.Context
import com.nihongo.app.data.database.entity.VocabularyEntity
import java.io.InputStream

class XlsxImporter(private val context: Context) {
    private val csvImporter = CsvImporter(context)

    fun importVocabulary(inputStream: InputStream): List<VocabularyEntity> {
        return csvImporter.importCsvFromStream(inputStream, "N5")
    }
}
