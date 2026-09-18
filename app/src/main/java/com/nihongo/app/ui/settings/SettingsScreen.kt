package com.nihongo.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val speechRate by viewModel.speechRate.collectAsState()
    val apiKey by viewModel.geminiApiKey.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("App Settings", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Gemini API Key (AI Test Generation)", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { viewModel.setGeminiApiKey(it) },
                    label = { Text("API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Leave blank to use built-in offline test generation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Text-to-Speech (TTS) Speed", style = MaterialTheme.typography.titleMedium)
                Text("Current speed: ${String.format("%.2f", speechRate)}x", style = MaterialTheme.typography.bodyMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f).forEach { rate ->
                        FilterChip(
                            selected = speechRate == rate,
                            onClick = { viewModel.setSpeechRate(rate) },
                            label = { Text("${rate}x") }
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("About Nihongo", style = MaterialTheme.typography.titleMedium)
                Text("Offline-first Japanese learning application with vocabulary database, adaptive flashcards, native TTS pronunciation, and offline AI test generation.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
