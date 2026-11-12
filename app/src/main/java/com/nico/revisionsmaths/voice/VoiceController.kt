package com.nico.revisionsmaths.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Reconnaissance vocale native Android (gratuite, sans IA ni clé API).
 * Tous les appels sont sérialisés sur le thread principal et le moteur est
 * recréé à chaque tentative : certains appareils (notamment Samsung) retournent
 * l'erreur client (5) si le moteur est réutilisé ou détruit trop brutalement.
 */
class VoiceController(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(onResult: (String?) -> Unit, onError: (errorCode: Int) -> Unit) {
        mainHandler.post { startInternal(onResult, onError) }
    }

    private fun startInternal(onResult: (String?) -> Unit, onError: (errorCode: Int) -> Unit) {
        if (!isAvailable) {
            onError(ERROR_RECOGNITION_UNAVAILABLE)
            return
        }
        destroyInternal()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        }
        val newRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        newRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val text = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                destroyInternal()
                onResult(text)
            }

            override fun onError(error: Int) {
                destroyInternal()
                onError(error)
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val startError = runCatching { newRecognizer.startListening(intent) }.exceptionOrNull()
        if (startError != null) {
            destroyInternal()
            onError(ERROR_START_FAILED)
            return
        }
        recognizer = newRecognizer
    }

    fun stopListening() {
        mainHandler.post { destroyInternal() }
    }

    private fun destroyInternal() {
        recognizer?.let { r ->
            runCatching { r.cancel() }
            runCatching { r.destroy() }
        }
        recognizer = null
    }

    companion object {
        const val ERROR_RECOGNITION_UNAVAILABLE = -1
        const val ERROR_START_FAILED = -2

        val TRANSIENT_ERRORS: Set<Int> = setOf(1, 2, 5, 6, 7, 11)

        fun friendlyMessage(errorCode: Int): String = when (errorCode) {
            ERROR_RECOGNITION_UNAVAILABLE ->
                "La reconnaissance vocale n'est pas disponible sur cet appareil."
            ERROR_START_FAILED ->
                "Le micro n'a pas pu démarrer."
            1, 2, 11 -> "Problème de réseau : la reconnaissance vocale n'a pas abouti."
            5 -> "Le micro s'est arrêté inopinément."
            6 -> "Aucune voix n'a été détectée."
            7 -> "La réponse n'a pas été comprise."
            8 -> "Le micro est déjà occupé."
            9 -> "L'autorisation du micro est manquante."
            else -> "Erreur de reconnaissance vocale (code $errorCode)."
        }
    }
}
