package com.ierusalem.androchat.core.voice_message.playback

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class AndroidAudioPlayer(private val context: Context): AudioPlayer {
    private var mediaPlayer : MediaPlayer? = null

    private var isPlaying = false

    val playTiming : Flow<Int> = flow{
        while (isPlaying){
            val currentPosition =  mediaPlayer?.currentPosition!!
            val duration = mediaPlayer?.duration!!
            val currentTiming = (currentPosition/duration)*100
            emit(currentTiming)
            delay(1000) //one second
        }
    }

    override fun playFile(file: File) {
        isPlaying = true
        MediaPlayer.create(context, file.toUri()).apply {
            mediaPlayer = this
            start()
        }
    }

    override fun onFinished(onFinished:() -> Unit) {
        mediaPlayer?.setOnCompletionListener {
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