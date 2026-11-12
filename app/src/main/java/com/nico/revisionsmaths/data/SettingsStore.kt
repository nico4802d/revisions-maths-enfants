package com.nico.revisionsmaths.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

enum class AiProvider(
    val label: String,
    val baseUrl: String,
    val chatModel: String,
    val supportsTts: Boolean
) {
    OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-4o-mini", true),
    MISTRAL("Mistral AI", "https://api.mistral.ai/v1", "mistral-small-latest", false);

    fun chatCompletionsUrl(): String = "$baseUrl/chat/completions"
    fun ttsUrl(): String = "$baseUrl/audio/speech"

    companion object {
        fun fromName(name: String?): AiProvider = entries.firstOrNull { it.name == name } ?: OPENAI
    }
}

class SettingsStore(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_settings",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var apiKey: String?
        get() = prefs.getString(KEY_API, null)?.takeIf { it.isNotBlank() }
        set(value) {
            if (value == null) prefs.edit().remove(KEY_API).apply()
            else prefs.edit().putString(KEY_API, value).apply()
        }

    var aiProvider: AiProvider
        get() = AiProvider.fromName(prefs.getString(KEY_PROVIDER, null))
        set(value) {
            prefs.edit().putString(KEY_PROVIDER, value.name).apply()
        }

    companion object {
        private const val KEY_API = "ai_api_key"
        private const val KEY_PROVIDER = "ai_provider"
    }
}
