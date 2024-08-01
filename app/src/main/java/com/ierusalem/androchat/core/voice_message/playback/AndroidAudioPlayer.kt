package com.ierusalem.androchat.core.voice_message.playback

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.ierusalem.androchat.core.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
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
    }.distinctUntilChangedBy { it }
        .flowOn(Dispatchers.IO)

    override fun playAudioFile(file: File, onFinished: () -> Unit) {
        stop() // Ensure any existing media player is stopped before starting a new one
        isPlaying = true
        MediaPlayer.create(context, file.toUri()).apply {
            mediaPlayer = this
            start()
        }
        mediaPlayer?.setOnCompletionListener {
            log("finished")
            stop()
            onFinished()
        }
    }

    override fun resumeAudioFile(file: File, currentPosition: Int, onFinished: () -> Unit) {
        isPlaying = true
        mediaPlayer?.start()
        mediaPlayer?.seekTo(currentPosition)
        mediaPlayer?.setOnCompletionListener {
            log("finished")
            stop()
            onFinished()
        }
    }

    override fun pause(): Int? {
        isPlaying = false
        mediaPlayer?.pause()
        return mediaPlayer?.currentPosition
    }

    override fun stop() {
        isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}