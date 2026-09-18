package com.nihongo.app.ai

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object TestValidator {
    fun validateQuestion(
        questionText: String,
        questionType: String,
        options: List<String>,
        correctAnswer: String,
        expectedLevel: String,
        levelOfTest: String
    ): ValidationResult {
        if (questionText.isBlank()) {
            return ValidationResult(false, "Question text cannot be blank.")
        }
        if (questionType !in listOf("VOCABULARY", "GRAMMAR", "LISTENING")) {
            return ValidationResult(false, "Invalid question type: $questionType")
        }
        if (options.size < 2) {
            return ValidationResult(false, "Question must have at least 2 options.")
        }
        if (correctAnswer.isBlank() || !options.contains(correctAnswer)) {
            return ValidationResult(false, "Correct answer must exist and match one of the options.")
        }
        if (expectedLevel != levelOfTest) {
            return ValidationResult(false, "JLPT level mismatch.")
        }
        return ValidationResult(true)
    }
}
