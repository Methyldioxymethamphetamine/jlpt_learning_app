package com.nihongo.app.ai

import com.nihongo.app.data.database.entity.VocabularyEntity

object PromptBuilder {
    fun buildPrompt(config: TestGenerationConfig, learnedVocabulary: List<VocabularyEntity>): String {
        val vocabContext = learnedVocabulary.take(30).joinToString("\n") { 
            "- ${it.japanese} (Reading: ${it.reading}, Romaji: ${it.romaji}, Type: ${it.type}, Meaning: ${it.meaning})" 
        }
        return """
            You are a JLPT Japanese test creator.
            Generate a realistic JLPT ${config.jlptLevel} practice test in JSON format.
            Use the user's learned vocabulary list below as the main source for target words and options:
            
            USER LEARNED VOCABULARY LIST:
            $vocabContext
            
            CONFIG CONSTRAINTS:
            - JLPT Level: ${config.jlptLevel}
            - Vocabulary Focus: ${config.vocabularyFocus.displayName}
            - Grammar Focus: ${config.grammarFocus.displayName}
            
            Provide a json response formatted exactly as follows:
            {
              "jlptLevel": "${config.jlptLevel}",
              "configSummary": "${config.jlptLevel} AI Generated Test",
              "questions": [
                {
                  "questionType": "VOCABULARY",
                  "questionText": "What is the meaning of 「言葉」?",
                  "listeningText": null,
                  "options": ["word", "book", "water", "tree"],
                  "correctAnswer": "word",
                  "explanation": "言葉 means word.",
                  "targetVocabulary": "言葉"
                },
                {
                  "questionType": "LISTENING",
                  "questionText": "Listen to the sentence and select what was eaten.",
                  "listeningText": "りんごを食べました。",
                  "options": ["Apple", "Bread", "Rice", "Water"],
                  "correctAnswer": "Apple",
                  "explanation": "りんご means apple.",
                  "targetVocabulary": "りんご"
                }
              ]
            }
        """.trimIndent()
    }
}
