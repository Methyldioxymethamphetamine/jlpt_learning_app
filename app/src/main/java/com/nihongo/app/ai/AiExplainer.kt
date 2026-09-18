package com.nihongo.app.ai

import android.content.Context
import com.nihongo.app.data.database.entity.TestQuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AiExplainer(context: Context) {
    private val prefs = context.getSharedPreferences("nihongo_prefs", Context.MODE_PRIVATE)

    suspend fun explainQuestion(
        question: TestQuestionEntity,
        userAnswer: String?,
        correctAnswer: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = prefs.getString("gemini_api_key", "") ?: ""

        if (apiKey.isBlank()) {
            return@withContext buildLocalExplanation(question, userAnswer, correctAnswer)
        }

        try {
            val prompt = """
                Explain this JLPT Japanese test question in clear English. Keep all Japanese words, hiragana/katakana, and kanji intact.
                
                Question: ${question.questionText}
                ${if (!question.listeningText.isNullOrBlank()) "Audio Script: ${question.listeningText}" else ""}
                User's Answer: ${userAnswer ?: "Not Answered"}
                Correct Answer: $correctAnswer
                Reference Note: ${question.explanation}
                
                Format your response clearly:
                1. Meaning / Context of the Japanese sentence/word.
                2. Why "$correctAnswer" is the correct answer.
                ${if (userAnswer != null && userAnswer != correctAnswer) "3. Why \"$userAnswer\" is incorrect." else ""}
            """.trimIndent()

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

                val root = JSONObject(responseStr)
                val text = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                text.trim()
            } else {
                buildLocalExplanation(question, userAnswer, correctAnswer)
            }
        } catch (e: Exception) {
            buildLocalExplanation(question, userAnswer, correctAnswer)
        }
    }

    private fun buildLocalExplanation(
        question: TestQuestionEntity,
        userAnswer: String?,
        correctAnswer: String
    ): String {
        val isCorrect = userAnswer == correctAnswer
        val sb = StringBuilder()
        sb.append("💡 AI Explanation:\n\n")
        sb.append("• Target Item: ${question.targetVocabulary ?: question.questionText}\n")
        sb.append("• Correct Answer: 「$correctAnswer」\n")
        if (userAnswer != null) {
            sb.append("• Your Answer: 「$userAnswer」 (${if (isCorrect) "Correct! ✅" else "Incorrect ❌"})\n\n")
        } else {
            sb.append("• Your Answer: Not Answered\n\n")
        }
        sb.append("Explanation:\n${question.explanation}\n\n")
        if (!isCorrect && userAnswer != null) {
            sb.append("Note: 「$userAnswer」 does not match the required JLPT grammar or vocabulary rule for this context.")
        } else if (isCorrect) {
            sb.append("Great job! You identified the correct Japanese expression.")
        }
        return sb.toString()
    }
}
