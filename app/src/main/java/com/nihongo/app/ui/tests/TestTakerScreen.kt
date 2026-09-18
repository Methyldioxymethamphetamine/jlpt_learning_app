package com.nihongo.app.ui.tests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nihongo.app.data.database.entity.TestWithQuestions

@Composable
fun TestTakerScreen(
    testId: Long,
    onBack: () -> Unit,
    viewModel: TestsViewModel = viewModel()
) {
    var testData by remember { mutableStateOf<TestWithQuestions?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswers by remember { mutableStateOf<MutableMap<Int, String>>(mutableMapOf()) }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    LaunchedEffect(testId) {
        testData = viewModel.getTest(testId)
    }

    val data = testData
    if (data == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val questions = data.questions
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("This test has no questions.")
        }
        return
    }

    if (isSubmitted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Test Results", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Score: $score / ${questions.size}", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("Back to Tests")
            }
        }
        return
    }

    val currentQ = questions[currentIndex]
    val selectedOption = userAnswers[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Question ${currentIndex + 1} of ${questions.size}", style = MaterialTheme.typography.bodyMedium)
                Badge { Text(currentQ.question.questionType) }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(currentQ.question.questionText, style = MaterialTheme.typography.titleLarge)

                    val audioText = currentQ.question.listeningText
                    if (currentQ.question.questionType == "LISTENING" && !audioText.isNullOrBlank()) {
                        Button(
                            onClick = { viewModel.playAudio(audioText) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play Audio")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Play Audio")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            currentQ.options.forEach { option ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOption == option.optionText) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedOption == option.optionText,
                            onClick = {
                                userAnswers = userAnswers.toMutableMap().apply {
                                    put(currentIndex, option.optionText)
                                }
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == option.optionText,
                            onClick = {
                                userAnswers = userAnswers.toMutableMap().apply {
                                    put(currentIndex, option.optionText)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option.optionText, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = { currentIndex-- },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Previous")
                }
            }

            Button(
                onClick = {
                    if (currentIndex + 1 < questions.size) {
                        currentIndex++
                    } else {
                        var calculatedScore = 0
                        val answerMap = mutableMapOf<Long, String>()
                        questions.forEachIndexed { idx, q ->
                            val chosen = userAnswers[idx]
                            if (chosen != null) {
                                answerMap[q.question.id] = chosen
                                if (chosen == q.question.correctAnswer) {
                                    calculatedScore++
                                }
                            }
                        }
                        score = calculatedScore
                        isSubmitted = true
                        viewModel.saveDetailedTestResults(testId, answerMap, calculatedScore)
                    }
                },
                enabled = selectedOption != null,
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text(if (currentIndex + 1 < questions.size) "Next" else "Submit")
            }
        }
    }
}
