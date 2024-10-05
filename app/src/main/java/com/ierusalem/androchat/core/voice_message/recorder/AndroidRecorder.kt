package com.ierusalem.androchat.core.voice_message.recorder

import java.io.File

interface AndroidRecorder {
    fun startAudio(outputFile: File)
    fun stopAudio()
}