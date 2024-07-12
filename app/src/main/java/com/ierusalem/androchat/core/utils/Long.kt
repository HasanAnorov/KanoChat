package com.ierusalem.androchat.core.utils

fun calculateDownloadPercentage(downloadedBytes: Long, totalBytes: Long): Double {
    if (totalBytes == 0L) {
        throw IllegalArgumentException("Total size of the file cannot be zero.")
    }
    return (downloadedBytes.toDouble() / totalBytes.toDouble()) * 100
}