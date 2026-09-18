package com.nihongo.app.ui.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nihongo.app.data.database.entity.QuestionWithOptions
import com.nihongo.app.data.database.entity.TestQuestionEntity
import com.nihongo.app.data.database.entity.TestWithQuestions
import com.nihongo.app.ui.tests.TestsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailScreen(
    testId: Long,
    onBack: () -> Unit,
    viewModel: TestsViewModel = viewModel()
) {
    var testData by remember { mutableStateOf<TestWithQuestions?>(null) }

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

    val test = data.test
    val questions = data.questions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(test.configSummary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Test Overview", style = MaterialTheme.typography.titleMedium)
                        Text("Level: ${test.jlptLevel} | Total Questions: ${questions.size}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "${test.score} / ${test.totalQuestions}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Questions Breakdown", style = MaterialTheme.typography.titleLarge)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(questions) { index, qWithOptions ->
                    QuestionReviewCard(
                        index = index + 1,
                        questionWithOptions = qWithOptions,
                        onPlayAudio = { text -> viewModel.playAudio(text) },
                        onAskAi = { question, userAnswer, correctAnswer ->
                            viewModel.explainQuestionWithAi(question, userAnswer, correctAnswer)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionReviewCard(
    index: Int,
    questionWithOptions: QuestionWithOptions,
    onPlayAudio: (String) -> Unit,
    onAskAi: suspend (TestQuestionEntity, String?, String) -> String
) {
    val q = questionWithOptions.question
    val scope = rememberCoroutineScope()
    var aiExplanation by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }

    val isCorrect = q.userAnswer == q.correctAnswer

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Q$index. ${q.questionType}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                Badge(
                    containerColor = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(if (isCorrect) "Correct ✅" else "Incorrect ❌")
                }
            }

            Text(q.questionText, style = MaterialTheme.typography.titleMedium)

            val audioText = q.listeningText
            if (q.questionType == "LISTENING" && !audioText.isNullOrBlank()) {
                Button(onClick = { onPlayAudio(audioText) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play Audio")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Audio")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("My Answer:", style = MaterialTheme.typography.bodyMedium)
                    SuggestionChip(
                        onClick = {},
                        label = { Text(q.userAnswer ?: "Not Answered") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        )
                    )
                }

                if (!isCorrect) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Correct Answer:", style = MaterialTheme.typography.bodyMedium)
                        SuggestionChip(
                            onClick = {},
                            label = { Text(q.correctAnswer) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        isLoadingAi = true
                        aiExplanation = onAskAi(q, q.userAnswer, q.correctAnswer)
                        isLoadingAi = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Ask AI", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoadingAi) "Asking AI..." else "Ask AI Explanation")
            }

            if (isLoadingAi) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val exp = aiExplanation
            if (exp != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(exp, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
