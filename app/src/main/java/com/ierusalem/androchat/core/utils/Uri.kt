package com.ierusalem.androchat.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.util.Locale

/**
 * Returns the MIME type of the given Uri.
 */
fun Uri.getMimeType(context: Context): String? {
    val mimeType: String?
    if (ContentResolver.SCHEME_CONTENT == this.scheme) {
        val cr: ContentResolver = context.contentResolver
        mimeType = cr.getType(this)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
            this.toString()
        )
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.lowercase(Locale.getDefault())
        )
    }
    return mimeType
}

fun Uri.getFileSizeInReadableFormat(contentResolver: ContentResolver): String {
    var fileSize = 0L
    this.let { returnUri ->
        contentResolver.query(returnUri, null, null, null, null)
    }?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        fileSize = cursor.getLong(sizeIndex)
    }
    return fileSize.readableFileSize()
}

fun Uri.getFileNameFromUri(contentResolver: ContentResolver): String {
    var fileName = "file"
    this.let { returnUri ->
        contentResolver.query(returnUri, null, null, null, null)
    }?.use { cursor ->
        val nameIndex =
            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        fileName = cursor.getString(nameIndex)
    }
    return fileName
}

fun Uri.getFileExtensionFromUri(contentResolver: ContentResolver): String {
    var fileName = "file"
    this.let { returnUri ->
        contentResolver.query(returnUri, null, null, null, null)
    }?.use { cursor ->
        val nameIndex =
            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        fileName = cursor.getString(nameIndex)
    }
    return fileName.getExtensionFromFilename()
}