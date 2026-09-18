package com.nihongo.app.ai

import android.content.Context
import com.nihongo.app.data.database.entity.VocabularyEntity
import com.nihongo.app.data.database.entity.VocabularyWithState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GeminiClient(context: Context) {
    private val prefs = context.getSharedPreferences("nihongo_prefs", Context.MODE_PRIVATE)

    suspend fun generateTest(
        config: TestGenerationConfig,
        learnedVocabulary: List<VocabularyEntity>,
        vocabWithStateList: List<VocabularyWithState>
    ): GeneratedTestPayload? {
        val apiKey = prefs.getString("gemini_api_key", "") ?: ""
        
        if (apiKey.isBlank()) {
            return LocalTestGenerator.generateTestFromLocalDb(config, vocabWithStateList)
        }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = PromptBuilder.buildPrompt(config, learnedVocabulary)
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.write(jsonBody.toString().toByteArray())
                
                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseStr = reader.readText()
                    reader.close()
                    parseGeminiResponse(responseStr, config.jlptLevel) ?: LocalTestGenerator.generateTestFromLocalDb(config, vocabWithStateList)
                } else {
                    LocalTestGenerator.generateTestFromLocalDb(config, vocabWithStateList)
                }
            } catch (e: Exception) {
                LocalTestGenerator.generateTestFromLocalDb(config, vocabWithStateList)
            }
        }
    }

    private fun parseGeminiResponse(responseStr: String, expectedLevel: String): GeneratedTestPayload? {
        try {
            val root = JSONObject(responseStr)
            val candidates = root.getJSONArray("candidates")
            val text = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            
            val jsonClean = text.substringAfter("```json").substringBefore("```").trim()
            val testJson = JSONObject(jsonClean)

            val jlptLevel = testJson.optString("jlptLevel", expectedLevel)
            val configSummary = testJson.optString("configSummary", "$jlptLevel AI Test")
            val questionsArray = testJson.getJSONArray("questions")
            
            val questions = mutableListOf<GeneratedQuestionPayload>()
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val qType = qObj.getString("questionType")
                val qText = qObj.getString("questionText")
                val listeningText = if (qObj.has("listeningText") && !qObj.isNull("listeningText")) qObj.getString("listeningText") else null
                val correctAnswer = qObj.getString("correctAnswer")
                val explanation = qObj.getString("explanation")
                val targetVocab = if (qObj.has("targetVocabulary") && !qObj.isNull("targetVocabulary")) qObj.getString("targetVocabulary") else null
                
                val optArray = qObj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optArray.length()) {
                    options.add(optArray.getString(j))
                }

                val validation = TestValidator.validateQuestion(qText, qType, options, correctAnswer, jlptLevel, expectedLevel)
                if (validation.isValid) {
                    questions.add(GeneratedQuestionPayload(qType, qText, listeningText, options, correctAnswer, explanation, targetVocab))
                }
            }

            if (questions.isNotEmpty()) {
                return GeneratedTestPayload(jlptLevel, configSummary, questions)
            }
        } catch (e: Exception) {
            // fallback
        }
        return null
    }
}

data class GeneratedTestPayload(
    val jlptLevel: String,
    val configSummary: String,
    val questions: List<GeneratedQuestionPayload>
)

data class GeneratedQuestionPayload(
    val questionType: String,
    val questionText: String,
    val listeningText: String?,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val targetVocabulary: String?
)
