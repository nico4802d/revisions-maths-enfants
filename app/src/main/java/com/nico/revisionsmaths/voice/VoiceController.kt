package com.nico.revisionsmaths.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Reconnaissance vocale native Android (gratuite, sans IA ni clé API).
 * Les codes d'erreur sont transmis à l'appelant pour permettre des
 * tentatives de relance automatiques sur les erreurs transitoires.
 */
class VoiceController(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(onResult: (String?) -> Unit, onError: (errorCode: Int) -> Unit) {
        if (!isAvailable) {
            onError(ERROR_RECOGNITION_UNAVAILABLE)
            return
        }
        stopListening()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    val text = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    stopListening()
                    onResult(text)
                }

                override fun onError(error: Int) {
                    stopListening()
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
            startListening(intent)
        }
    }

    fun stopListening() {
        recognizer?.destroy()
        recognizer = null
    }

    companion object {
        const val ERROR_RECOGNITION_UNAVAILABLE = -1

        val TRANSIENT_ERRORS: Set<Int> = setOf(5, 6, 7, 11)

        fun friendlyMessage(errorCode: Int): String = when (errorCode) {
            ERROR_RECOGNITION_UNAVAILABLE ->
                "La reconnaissance vocale n'est pas disponible sur cet appareil."
            1, 2, 11 -> "Problème de réseau : la reconnaissance vocale n'a pas abouti."
            5 -> "Le micro s'est interrompu inopinément."
            6 -> "Aucune voix n'a été détectée."
            7 -> "La réponse n'a pas été comprise."
            8 -> "Le micro est déjà occupé."
            9 -> "L'autorisation du micro est manquante."
            else -> "Erreur de reconnaissance vocale (code $errorCode)."
        }
    }
}
