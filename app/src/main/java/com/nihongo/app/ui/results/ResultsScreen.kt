package com.nihongo.app.ui.results

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nihongo.app.data.database.entity.TestWithQuestions
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ResultsScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: ResultsViewModel = viewModel()
) {
    val completedTests by viewModel.completedTests.collectAsState()
    val totalTests by viewModel.totalTestsTaken.collectAsState()
    val avgScore by viewModel.averageScorePercentage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Test Results & Statistics", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tests Completed", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalTests", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Avg Score", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${avgScore.toInt()}%", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Test History", style = MaterialTheme.typography.titleLarge)

        if (completedTests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No completed test results yet. Take a practice test to see your history.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(completedTests) { testWithQuestions ->
                    ResultCard(
                        testWithQuestions = testWithQuestions,
                        onClick = { onNavigateToDetail(testWithQuestions.test.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(testWithQuestions: TestWithQuestions, onClick: () -> Unit) {
    val test = testWithQuestions.test
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateStr = dateFormatter.format(Date(test.createdAt))
    val percentage = if (test.totalQuestions > 0) (test.score.toFloat() / test.totalQuestions) * 100 else 0f

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
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(text = test.configSummary, style = MaterialTheme.typography.titleMedium)
                }
                Text(text = "Completed: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Badge { Text(test.jlptLevel) }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${test.score}/${test.totalQuestions} (${percentage.toInt()}%)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
