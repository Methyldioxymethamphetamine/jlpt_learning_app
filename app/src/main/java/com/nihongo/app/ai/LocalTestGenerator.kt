package com.nihongo.app.ai

import com.nihongo.app.data.database.entity.VocabularyWithState

object LocalTestGenerator {

    fun generateTestFromLocalDb(
        config: TestGenerationConfig,
        vocabWithStateList: List<VocabularyWithState>
    ): GeneratedTestPayload {
        val level = config.jlptLevel
        val levelVocab = vocabWithStateList.filter { it.vocabulary.jlptLevel.equals(level, ignoreCase = true) }
            .ifEmpty { vocabWithStateList }

        if (levelVocab.isEmpty()) {
            return fallbackMockTest(level)
        }

        val sortedVocab = when (config.vocabularyFocus) {
            VocabularyFocus.WEAK_WORDS, VocabularyFocus.DIFFICULT ->
                levelVocab.sortedBy { it.learningState?.masteryScore ?: 0f }
            VocabularyFocus.FREQUENT ->
                levelVocab.sortedByDescending { it.learningState?.timesSeen ?: 0 }
            VocabularyFocus.RARE ->
                levelVocab.sortedBy { it.learningState?.timesSeen ?: 0 }
            VocabularyFocus.RECENTLY_LEARNED ->
                levelVocab.sortedByDescending { it.learningState?.lastSeen ?: 0L }
            VocabularyFocus.RANDOM, VocabularyFocus.HIGHLY_MASTERED ->
                levelVocab.shuffled()
        }

        val questions = mutableListOf<GeneratedQuestionPayload>()
        val count = config.questionCount.coerceIn(3, 50)

        val targetPool = sortedVocab.map { it.vocabulary }.distinctBy { it.japanese }
        val allMeanings = targetPool.map { it.meaning }.filter { it.isNotBlank() }.distinct()
        val allReadings = targetPool.map { it.reading }.filter { it.isNotBlank() }.distinct()
        val allKanji = targetPool.map { it.japanese }.filter { it.isNotBlank() }.distinct()

        var poolIndex = 0

        for (i in 0 until count) {
            val qType = when (config.categoryMode) {
                TestCategoryMode.VOCAB_GRAMMAR -> if (i % 2 == 0) "VOCABULARY" else "GRAMMAR"
                TestCategoryMode.LISTENING_ONLY -> "LISTENING"
                TestCategoryMode.COMBINED -> when (i % 3) {
                    0 -> "VOCABULARY"
                    1 -> "GRAMMAR"
                    else -> "LISTENING"
                }
            }

            val target = targetPool.getOrNull(poolIndex % targetPool.size) ?: targetPool.random()
            poolIndex++

            when (qType) {
                "VOCABULARY" -> {
                    val subType = i % 3
                    when (subType) {
                        0 -> {
                            val correct = target.reading
                            val wrongOptions = allReadings.filter { it != correct }.shuffled().take(3)
                            val options = (wrongOptions + correct).shuffled()

                            questions.add(
                                GeneratedQuestionPayload(
                                    questionType = "VOCABULARY",
                                    questionText = "「${target.japanese}」の正しい読み方を一つ選びなさい。",
                                    listeningText = null,
                                    options = options,
                                    correctAnswer = correct,
                                    explanation = "「${target.japanese}」の読み方は「${target.reading}」です。意味：${target.meaning}。",
                                    targetVocabulary = target.japanese
                                )
                            )
                        }
                        1 -> {
                            val correct = target.meaning
                            val wrongOptions = allMeanings.filter { it != correct }.shuffled().take(3)
                            val options = (wrongOptions + correct).shuffled()

                            questions.add(
                                GeneratedQuestionPayload(
                                    questionType = "VOCABULARY",
                                    questionText = "「${target.japanese}」 (${target.reading}) の正しい意味を一つ選びなさい。",
                                    listeningText = null,
                                    options = options,
                                    correctAnswer = correct,
                                    explanation = "「${target.japanese}」 (${target.reading}) の意味は '${target.meaning}' です。",
                                    targetVocabulary = target.japanese
                                )
                            )
                        }
                        else -> {
                            val correct = target.japanese
                            val wrongOptions = allKanji.filter { it != correct }.shuffled().take(3)
                            val options = (wrongOptions + correct).shuffled()

                            questions.add(
                                GeneratedQuestionPayload(
                                    questionType = "VOCABULARY",
                                    questionText = "「${target.reading}」の漢字表記として正しいものを一つ選びなさい。",
                                    listeningText = null,
                                    options = options,
                                    correctAnswer = correct,
                                    explanation = "「${target.reading}」の漢字表記は「${target.japanese}」です。",
                                    targetVocabulary = target.japanese
                                )
                            )
                        }
                    }
                }
                "GRAMMAR" -> {
                    val grammarQuestions = getJlptGrammarQuestions(level)
                    val gq = grammarQuestions.random()
                    questions.add(
                        GeneratedQuestionPayload(
                            questionType = "GRAMMAR",
                            questionText = gq.sentence,
                            listeningText = null,
                            options = gq.options,
                            correctAnswer = gq.correctAnswer,
                            explanation = gq.explanation,
                            targetVocabulary = null
                        )
                    )
                }
                "LISTENING" -> {
                    val scenario = getJlptListeningScenario(target.japanese, target.meaning, level)
                    questions.add(
                        GeneratedQuestionPayload(
                            questionType = "LISTENING",
                            questionText = scenario.questionPrompt,
                            listeningText = scenario.audioText,
                            options = scenario.options,
                            correctAnswer = scenario.correctAnswer,
                            explanation = scenario.explanation,
                            targetVocabulary = target.japanese
                        )
                    )
                }
            }
        }

        val categoryTitle = when (config.categoryMode) {
            TestCategoryMode.VOCAB_GRAMMAR -> "Vocab & Grammar"
            TestCategoryMode.LISTENING_ONLY -> "Listening"
            TestCategoryMode.COMBINED -> "Combined"
        }

        return GeneratedTestPayload(
            jlptLevel = level,
            configSummary = "$level $categoryTitle ($count Questions)",
            questions = questions
        )
    }

    private data class GrammarTemplate(
        val sentence: String,
        val options: List<String>,
        val correctAnswer: String,
        val explanation: String
    )

    private fun getJlptGrammarQuestions(level: String): List<GrammarTemplate> {
        return listOf(
            GrammarTemplate(
                sentence = "私＿日本語の勉強が好きです。",
                options = listOf("は", "が", "を", "に").shuffled(),
                correctAnswer = "は",
                explanation = "「は」は文の主題（私）を表す助詞です。"
            ),
            GrammarTemplate(
                sentence = "毎朝、７時＿起きます。",
                options = listOf("に", "で", "を", "から").shuffled(),
                correctAnswer = "に",
                explanation = "特定の実時間を表すときは「に」を使います。"
            ),
            GrammarTemplate(
                sentence = "図書館＿静かに本を読みます。",
                options = listOf("で", "に", "へ", "を").shuffled(),
                correctAnswer = "で",
                explanation = "動作が行われる場所を表す助詞は「で」です。"
            ),
            GrammarTemplate(
                sentence = "明日、友達＿一緒に映画を見に行きます。",
                options = listOf("と", "に", "で", "を").shuffled(),
                correctAnswer = "と",
                explanation = "共同で動作を行う相手を表す助詞は「と」です。"
            ),
            GrammarTemplate(
                sentence = "次の文の＿★＿に入るものはどれですか。\n「私 は ＿ ＿ ★ ＿ 行きます」",
                options = listOf("学校へ", "バスで", "毎日", "一人で").shuffled(),
                correctAnswer = "学校へ",
                explanation = "正解の並び順：「私 は 毎日 一人で 学校へ 行きます」で、★の位置は「学校へ」になります。"
            )
        )
    }

    private data class ListeningScenario(
        val questionPrompt: String,
        val audioText: String,
        val options: List<String>,
        val correctAnswer: String,
        val explanation: String
    )

    private fun getJlptListeningScenario(word: String, meaning: String, level: String): ListeningScenario {
        val audio = "男の人と女の人が話しています。\n男：すみません、「$word」の意味を教えてください。\n女：はい、「$word」は「$meaning」という意味ですよ。\n男：ありがとうございます。よく分かりました。"
        val correct = meaning
        val options = listOf(
            meaning,
            "Station; train stop",
            "Weather; temperature",
            "Time; schedule"
        ).shuffled()

        return ListeningScenario(
            questionPrompt = "音声の会話を聞いて、「$word」の意味として正しいものを一つ選びなさい。",
            audioText = audio,
            options = options,
            correctAnswer = correct,
            explanation = "会話の中で、女の人が「$word」は「$meaning」という意味だと説明しています。"
        )
    }

    private fun fallbackMockTest(level: String): GeneratedTestPayload {
        return GeneratedTestPayload(
            jlptLevel = level,
            configSummary = "$level Practice Test",
            questions = listOf(
                GeneratedQuestionPayload(
                    questionType = "VOCABULARY",
                    questionText = "「本」 (ほん) の意味として最もよいものを一つ選びなさい。",
                    listeningText = null,
                    options = listOf("Book", "Water", "Tree", "Person"),
                    correctAnswer = "Book",
                    explanation = "本 means book.",
                    targetVocabulary = "本"
                )
            )
        )
    }
}
