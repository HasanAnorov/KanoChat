package com.ierusalem.androchat.core.voice_message.playback

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.ierusalem.androchat.core.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class AndroidAudioPlayer(private val context: Context): AudioPlayer {

    private var mediaPlayer : MediaPlayer? = null
    private var isPlaying = false

    val playTiming: Flow<Long> = flow {
        while (isPlaying) {
            mediaPlayer?.let {
                val currentPosition = it.currentPosition
                val duration = it.duration
                val currentTiming = if (duration > 0) {
                    (currentPosition.toFloat() / duration * 100).toLong()
                } else {
                    0L
                }
                emit(currentTiming)
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun playFile(file: File, onFinished: () -> Unit) {
        stop() // Ensure any existing media player is stopped before starting a new one
        isPlaying = true
        mediaPlayer = MediaPlayer.create(context, file.toUri()).apply {
            setOnPreparedListener { start() }
        }
        mediaPlayer?.setOnCompletionListener {
            log("finished")
            stop()
            onFinished()
        }
    }

    override fun pause() {
        isPlaying = false
        mediaPlayer?.pause()
    }

    override fun stop() {
        isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}