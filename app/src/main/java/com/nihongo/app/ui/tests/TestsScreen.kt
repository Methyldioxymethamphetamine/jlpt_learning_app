package com.nihongo.app.ui.tests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nihongo.app.ai.TestCategoryMode
import com.nihongo.app.ai.TestGenerationConfig
import com.nihongo.app.ai.VocabularyFocus
import com.nihongo.app.data.database.entity.TestWithQuestions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    onNavigateToTest: (Long) -> Unit,
    viewModel: TestsViewModel = viewModel()
) {
    val tests by viewModel.allTests.collectAsState()
    var selectedPanelTab by remember { mutableIntStateOf(0) }
    var showGenerateDialog by remember { mutableStateOf(false) }

    var selectedLevel by remember { mutableStateOf("N4") }
    var selectedCategoryMode by remember { mutableStateOf(TestCategoryMode.COMBINED) }
    var questionCount by remember { mutableFloatStateOf(20f) }
    var vocabFocus by remember { mutableStateOf(VocabularyFocus.WEAK_WORDS) }

    val filteredTests = tests.filter { testWithQ ->
        val summary = testWithQ.test.configSummary
        if (selectedPanelTab == 0) {
            !summary.contains("Listening", ignoreCase = true) || summary.contains("Combined", ignoreCase = true)
        } else {
            summary.contains("Listening", ignoreCase = true) || summary.contains("Combined", ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showGenerateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Generate Test")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("JLPT Practice Tests", style = MaterialTheme.typography.headlineMedium)

            TabRow(selectedTabIndex = selectedPanelTab) {
                Tab(
                    selected = selectedPanelTab == 0,
                    onClick = { selectedPanelTab = 0 },
                    text = { Text("Vocab & Grammar") },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) }
                )
                Tab(
                    selected = selectedPanelTab == 1,
                    onClick = { selectedPanelTab = 1 },
                    text = { Text("Listening Series") },
                    icon = { Icon(Icons.Default.VolumeUp, contentDescription = null) }
                )
            }

            if (filteredTests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedPanelTab == 0) "No Vocab & Grammar tests yet. Tap '+' to generate one."
                        else "No Listening series tests yet. Tap '+' to generate one.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredTests) { testWithQuestions ->
                        TestCard(testWithQuestions, onClick = { onNavigateToTest(testWithQuestions.test.id) })
                    }
                }
            }
        }

        if (showGenerateDialog) {
            AlertDialog(
                onDismissRequest = { showGenerateDialog = false },
                title = { Text("Generate AI Practice Test") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("JLPT Level:", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("N5", "N4", "N3").forEach { lvl ->
                                FilterChip(
                                    selected = selectedLevel == lvl,
                                    onClick = { selectedLevel = lvl },
                                    label = { Text(lvl) }
                                )
                            }
                        }

                        Text("Test Category Mode:", style = MaterialTheme.typography.titleSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TestCategoryMode.values().forEach { mode ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedCategoryMode = mode }
                                ) {
                                    RadioButton(
                                        selected = selectedCategoryMode == mode,
                                        onClick = { selectedCategoryMode = mode }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(mode.displayName, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Text("Number of Questions: ${questionCount.toInt()} (Max 50)", style = MaterialTheme.typography.titleSmall)
                        Slider(
                            value = questionCount,
                            onValueChange = { questionCount = it },
                            valueRange = 5f..50f,
                            steps = 8
                        )

                        Text("Vocabulary Focus:", style = MaterialTheme.typography.titleSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            VocabularyFocus.values().take(4).forEach { focus ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { vocabFocus = focus }
                                ) {
                                    RadioButton(
                                        selected = vocabFocus == focus,
                                        onClick = { vocabFocus = focus }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(focus.displayName, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val config = TestGenerationConfig(
                            jlptLevel = selectedLevel,
                            questionCount = questionCount.toInt(),
                            categoryMode = selectedCategoryMode,
                            vocabularyFocus = vocabFocus
                        )
                        viewModel.generateTest(config)
                        showGenerateDialog = false
                    }) {
                        Text("Generate")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGenerateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TestCard(testWithQuestions: TestWithQuestions, onClick: () -> Unit) {
    val test = testWithQuestions.test
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateStr = dateFormatter.format(Date(test.createdAt))

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (test.configSummary.contains("Listening", ignoreCase = true)) Icons.Default.VolumeUp else Icons.Default.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(text = test.configSummary, style = MaterialTheme.typography.titleMedium)
                }
                Text(text = "Created: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Badge { Text(test.jlptLevel) }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (test.isCompleted) "Score: ${test.score}/${test.totalQuestions}" else "Not Taken",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (test.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
