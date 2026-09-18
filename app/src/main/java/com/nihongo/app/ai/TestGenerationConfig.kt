package com.nihongo.app.ai

enum class TestCategoryMode(val displayName: String) {
    VOCAB_GRAMMAR("Vocab + Grammar Only"),
    LISTENING_ONLY("Listening Only"),
    COMBINED("Combined (Vocab + Grammar + Listening)")
}

data class TestGenerationConfig(
    val jlptLevel: String = "N4",
    val questionCount: Int = 20,
    val categoryMode: TestCategoryMode = TestCategoryMode.COMBINED,
    val vocabularyFocus: VocabularyFocus = VocabularyFocus.WEAK_WORDS,
    val grammarFocus: GrammarFocus = GrammarFocus.RECENTLY_STUDIED
)

enum class VocabularyFocus(val displayName: String) {
    WEAK_WORDS("New / Weak Words"),
    FREQUENT("Frequently Appearing"),
    RARE("Rarely Appearing"),
    RECENTLY_LEARNED("Recently Learned Words"),
    HIGHLY_MASTERED("Highly Mastered Words"),
    DIFFICULT("Difficult Words"),
    RANDOM("Random Learned Words")
}

enum class GrammarFocus(val displayName: String) {
    RECENTLY_STUDIED("Recently Studied Grammar"),
    COMMON("Common Grammar Points"),
    ADVANCED("Challenging Grammar")
}
