package com.ierusalem.androchat.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.util.Locale

/**
 * Returns the MIME type of the given Uri.
 */
fun Uri.getMimeType(context: Context): String? {
    var mimeType: String? = null
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