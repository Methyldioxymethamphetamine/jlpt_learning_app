package com.nihongo.app.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("nihongo_prefs", Context.MODE_PRIVATE)

    private val _speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 1.0f))
    val speechRate: StateFlow<Float> = _speechRate

    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        prefs.edit().putFloat("speech_rate", rate).apply()
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        prefs.edit().putString("gemini_api_key", key).apply()
    }
}
