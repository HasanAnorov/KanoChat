package com.ierusalem.androchat.core.voice_message.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.ierusalem.androchat.core.utils.log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AndroidAudioRecorder(
    private val context: Context
): AndroidRecorder {

    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun startAudio(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            try{
                prepare()
                start()
            }catch (e: IOException){
                e.printStackTrace()
            }
            recorder = this
        }
    }

    override fun stopAudio() {
        try {
            recorder?.stop()
            recorder = null
        }catch (e: RuntimeException){
            e.printStackTrace()
            log("runtime exception")
            recorder?.reset()
        }
    }

}