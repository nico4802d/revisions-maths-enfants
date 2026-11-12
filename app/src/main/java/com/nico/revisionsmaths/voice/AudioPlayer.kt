package com.nico.revisionsmaths.voice

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Lecture des réponses audio MP3 renvoyées par l'API de synthèse vocale IA.
 */
class AudioPlayer {

    private var player: MediaPlayer? = null

    suspend fun play(context: Context, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
            suspendCancellableCoroutine { continuation ->
                runCatching {
                    file.writeBytes(bytes)
                    MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        setOnCompletionListener {
                            it.release()
                            file.delete()
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                        setOnErrorListener { mp, _, _ ->
                            mp.release()
                            file.delete()
                            if (continuation.isActive) continuation.resume(Unit)
                            true
                        }
                        prepare()
                        start()
                        player = this
                    }
                }.onFailure {
                    file.delete()
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
    }

    fun stop() {
        player?.release()
        player = null
    }
}
