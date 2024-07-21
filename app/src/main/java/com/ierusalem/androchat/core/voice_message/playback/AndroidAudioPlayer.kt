package com.ierusalem.androchat.core.voice_message.playback

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AndroidAudioPlayer(private val context: Context): AudioPlayer {
    private var mediaPlayer : MediaPlayer? = null

    override fun playFile(file: File) {
        MediaPlayer.create(context, file.toUri()).apply {
            mediaPlayer = this
            start()
        }
    }

    override fun pause() {

    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}