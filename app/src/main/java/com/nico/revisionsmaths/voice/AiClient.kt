package com.nico.revisionsmaths.voice

import com.nico.revisionsmaths.data.AiProvider
import com.nico.revisionsmaths.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Client IA générique : fonctionne avec OpenAI ou Mistral AI
 * (API chat-completions compatibles). La clé API est fournie
 * par l'utilisateur dans les réglages et n'est jamais versionnée.
 * La synthèse vocale n'est disponible que pour OpenAI ; sinon
 * le TTS natif Android est utilisé.
 */
class AiClient(private val settingsStore: SettingsStore) {

    val isConfigured: Boolean
        get() = settingsStore.apiKey != null

    private val provider: AiProvider get() = settingsStore.aiProvider

    suspend fun chat(systemPrompt: String, userMessage: String): String? =
        withContext(Dispatchers.IO) {
            val key = settingsStore.apiKey ?: return@withContext null
            runCatching {
                val messages = JSONArray().apply {
                    put(JSONObject().put("role", "system").put("content", systemPrompt))
                    put(JSONObject().put("role", "user").put("content", userMessage))
                }
                val body = JSONObject()
                    .put("model", provider.chatModel)
                    .put("messages", messages)
                    .put("max_tokens", 150)
                    .put("temperature", 0.7)
                val json = postJson(provider.chatCompletionsUrl(), key, body)
                json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }.getOrNull()
        }

    suspend fun speech(text: String): ByteArray? = withContext(Dispatchers.IO) {
        val key = settingsStore.apiKey ?: return@withContext null
        if (!provider.supportsTts) return@withContext null
        runCatching {
            val body = JSONObject()
                .put("model", "gpt-4o-mini-tts")
                .put("voice", "coral")
                .put("input", text)
                .put("response_format", "mp3")
            postForBytes(provider.ttsUrl(), key, body)
        }.getOrNull()
    }

    private fun postJson(url: String, apiKey: String, body: JSONObject): JSONObject {
        val text = post(url, apiKey, body)
        return JSONObject(text)
    }

    private fun postForBytes(url: String, apiKey: String, body: JSONObject): ByteArray {
        val conn = open(url, apiKey, body)
        val bytes = conn.inputStream.readBytes()
        conn.disconnect()
        return bytes
    }

    private fun post(url: String, apiKey: String, body: JSONObject): String {
        val conn = open(url, apiKey, body)
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        return text
    }

    private fun open(url: String, apiKey: String, body: JSONObject): HttpURLConnection {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 8_000
            readTimeout = 12_000
            doOutput = true
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val code = conn.responseCode
        if (code != HttpURLConnection.HTTP_OK) {
            conn.disconnect()
            throw IOException("HTTP $code")
        }
        return conn
    }
}
