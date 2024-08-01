package com.ierusalem.androchat.core.voice_message.playback

import java.io.File

interface AudioPlayer {
    fun playAudioFile(file: File, onFinished: () -> Unit)
    fun resumeAudioFile(file: File, currentPosition:Int, onFinished: () -> Unit)
    fun pause():Int?
    fun stop()
}