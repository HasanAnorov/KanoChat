package com.ierusalem.androchat.features_tcp.server

import android.os.Build
import androidx.annotation.CheckResult
import androidx.annotation.ChecksSdkIntAtLeast

object ServerDefaults {

    @JvmStatic
    @CheckResult
    fun getSsidPrefix(): String {
        return "DIRECT-TF-"
    }

    @JvmStatic
    @CheckResult
    fun asSsid(ssid: String): String {
        return "${getSsidPrefix()}${ssid}"
    }

    @CheckResult
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun canUseCustomConfig(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

}