package com.nihongo.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onNavigateToStudy: () -> Unit,
    onNavigateToTests: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Nihongo", style = MaterialTheme.typography.headlineMedium)

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Overall Learning Progress", style = MaterialTheme.typography.titleLarge)

                val progress = if (stats.totalWords > 0) stats.learnedWords.toFloat() / stats.totalWords else 0f

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 14.dp,
                        strokeCap = StrokeCap.Round,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${stats.learnedWords} / ${stats.totalWords}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "words learned",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Today, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Learned / Reviewed Today", style = MaterialTheme.typography.titleMedium)
                        Text("Keep up your daily study streak!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Text(
                    text = "${stats.learnedToday}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Progress by JLPT Level", style = MaterialTheme.typography.titleMedium)

                LevelProgressRow("N5", stats.n5Learned, stats.n5Total)
                LevelProgressRow("N4", stats.n4Learned, stats.n4Total)
                LevelProgressRow("N3", stats.n3Learned, stats.n3Total)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToStudy,
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Default.School, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Study")
            }
            OutlinedButton(
                onClick = onNavigateToTests,
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Practice Tests")
            }
        }
    }
}

@Composable
fun LevelProgressRow(level: String, learned: Int, total: Int) {
    val progress = if (total > 0) learned.toFloat() / total else 0f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(level, style = MaterialTheme.typography.titleSmall)
            Text("$learned / $total words (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            strokeCap = StrokeCap.Round
        )
    }
}
