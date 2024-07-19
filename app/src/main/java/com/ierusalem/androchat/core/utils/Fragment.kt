package com.ierusalem.androchat.core.utils

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", activity?.packageName, null)
    ).also(::startActivity)
}

fun Fragment.shortToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(requireContext(), text, duration).apply {
        setGravity(Gravity.CENTER, 0, 0)
        show()
    }
}