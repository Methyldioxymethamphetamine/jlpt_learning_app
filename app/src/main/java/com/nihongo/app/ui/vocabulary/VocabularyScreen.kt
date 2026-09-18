package com.nihongo.app.ui.vocabulary

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nihongo.app.data.database.entity.VocabularyWithState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(viewModel: VocabularyViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val vocabularyItems by viewModel.vocabularyList.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { inputStream ->
                viewModel.importXlsx(inputStream)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                filePickerLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel"))
            }) {
                Icon(Icons.Default.UploadFile, contentDescription = "Import XLSX")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null, "N5", "N4", "N3").forEach { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { viewModel.selectLevel(level) },
                        label = { Text(level ?: "All") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (vocabularyItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No vocabulary found. Tap the upload button to import an XLSX file.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(vocabularyItems) { item ->
                        VocabularyCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun VocabularyCard(item: VocabularyWithState) {
    val vocab = item.vocabulary
    val state = item.learningState

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = vocab.japanese,
                    style = MaterialTheme.typography.titleLarge
                )
                Badge {
                    Text(vocab.jlptLevel)
                }
            }
            Text(
                text = "${vocab.reading} (${vocab.romaji})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${vocab.type.replaceFirstChar { it.uppercase() }}: ${vocab.meaning}",
                style = MaterialTheme.typography.bodyLarge
            )
            if (state != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mastery: ${(state.masteryScore * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Text("Streak: ${state.streak}", style = MaterialTheme.typography.bodySmall)
                    Text("State: ${state.learningState}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
