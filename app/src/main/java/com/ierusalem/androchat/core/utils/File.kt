package com.ierusalem.androchat.core.utils

import android.media.MediaMetadataRetriever
import java.io.File

fun File.getAudioFileDuration():Long{
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(this.path)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        durationString?.toLong() ?: 0L
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    } finally {
        retriever.release()
    }
}