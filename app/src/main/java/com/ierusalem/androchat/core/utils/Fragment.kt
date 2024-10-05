package com.ierusalem.androchat.core.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.utils.Constants.FILE_PROVIDER_AUTHORITY
import com.ierusalem.androchat.core.utils.Constants.generateUniqueFileName
import java.io.File
import java.io.FileOutputStream

fun Fragment.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", activity?.packageName, null)
    ).also(::startActivity)
}

fun Fragment.shortToast(text: String, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(requireContext(), text, duration).apply {
        setGravity(Gravity.CENTER, 0, 0)
        show()
    }
}

fun Fragment.longToast(text: String, duration: Int = Toast.LENGTH_LONG): Toast {
    return Toast.makeText(requireContext(), text, duration).apply {
        setGravity(Gravity.CENTER, 0, 0)
        show()
    }
}

fun Fragment.openWifiSettings() {
    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
    startActivity(intent)
}

fun Fragment.makeCall(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.setData(Uri.parse("tel:$phoneNumber"))
    startActivity(intent)
}

fun Fragment.openFile(fileName: String, resourceDirectory: File) {
    try {
        val file = File(resourceDirectory, fileName)
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            FILE_PROVIDER_AUTHORITY,
            file
        )
        val mimeType = uri.getMimeType(requireContext())
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.open_with))
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // no Activity to handle this kind of files
        shortToast(getString(R.string.no_application_found_to_open_this_file))
        log("can not open a file !")
        e.printStackTrace()
    }
}

fun Fragment.generateFileFromUri(uri: Uri, resourceDirectory: File): File {
    val contentResolver = activity!!.contentResolver

    if (!resourceDirectory.exists()) {
        resourceDirectory.mkdir()
    }

    val fileName = uri.getFileNameFromUri(contentResolver)
    val fileNameWithLabel = fileName.addLabelBeforeExtension()
    var file = File(resourceDirectory, fileNameWithLabel)
    if (file.exists()) {
        val fileNameWithoutExt = fileNameWithLabel.getFileNameWithoutExtension()
        val uniqueFileName =
            generateUniqueFileName(
                resourceDirectory.toString(),
                fileNameWithoutExt,
                file.extension
            )
        file = File(uniqueFileName)
    }

    val inputStream = requireContext().contentResolver.openInputStream(uri)
    val fileOutputStream = FileOutputStream(file)
    inputStream?.copyTo(fileOutputStream)
    fileOutputStream.close()
    return file
}
