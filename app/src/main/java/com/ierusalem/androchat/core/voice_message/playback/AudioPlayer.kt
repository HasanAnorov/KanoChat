package com.ierusalem.androchat.core.voice_message.playback

import java.io.File

interface AudioPlayer {
    fun playFile(file: File, onFinished: () -> Unit)
    fun pause()
    fun stop()
}