package com.nico.revisionsmaths.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Synthèse vocale native Android (gratuite, sans IA ni clé API).
 * Utilisée par défaut, et en secours quand l'IA n'est pas configurée
 * ou ne propose pas de synthèse vocale.
 */
class NativeTts(context: Context) : TextToSpeech.OnInitListener {

    private var engine: TextToSpeech? = null
    private var ready = false

    init {
        engine = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            engine?.language = Locale.FRENCH
            ready = true
        }
    }

    suspend fun speak(text: String) {
        val tts = engine ?: return
        if (!ready) {
            // Le moteur TTS peut être lent à s'initialiser : on attend jusqu'à 4 s
            // en petits pas avant d'abandonner (le message serait sinon ignoré).
            repeat(8) {
                if (ready) return@repeat
                delay(500)
            }
            if (!ready) return
        }
        suspendCancellableCoroutine { continuation ->
            val utteranceId = UUID.randomUUID().toString()
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onError(utteranceId: String?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            })
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
        }
    }

    fun shutdown() {
        engine?.shutdown()
        engine = null
    }

    fun stopSpeaking() {
        engine?.stop()
    }
}
