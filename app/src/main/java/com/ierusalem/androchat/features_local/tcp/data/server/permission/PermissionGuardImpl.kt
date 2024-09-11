package com.ierusalem.androchat.features_local.tcp.data.server.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.CheckResult
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PermissionGuardImpl(
    private val context: Context,
) : PermissionGuard {

    @CheckResult
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context.applicationContext, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    override val requiredPermissionsForContacts: List<String> by lazy(LazyThreadSafetyMode.NONE) {
        CONTACTS_READ_PERMISSIONS
    }

    override val requiredPermissionsForLocalOnlyHotSpot: List<String>
        get() = WIFI_NEARBY_PERMISSIONS

    override val requiredPermissionsForRecordingAudio: List<String>
        get() = AUDIO_RECORDING_PERMISSIONS

    override val requiredPermissionsForWifi: List<String> by lazy(LazyThreadSafetyMode.NONE) {
        // Always require these WiFi permissions
        WIFI_NEARBY_PERMISSIONS
    }

    override suspend fun canAccessContacts(): Boolean =
        withContext(context = Dispatchers.Main) {
            return@withContext requiredPermissionsForContacts.all {
                hasPermission(it)
            }
        }

    override suspend fun canCreateLocalOnlyHotSpotNetwork(): Boolean =
        withContext(context = Dispatchers.Main) {
            return@withContext requiredPermissionsForLocalOnlyHotSpot.all {
                hasPermission(it)
            }
        }

    override suspend fun canRecordAudio(): Boolean =
        withContext(context = Dispatchers.Main) {
            return@withContext requiredPermissionsForRecordingAudio.all {
                hasPermission(it)
            }
        }

    override suspend fun canCreateNetwork(): Boolean =
        withContext(context = Dispatchers.Main) {
            return@withContext requiredPermissionsForWifi.all {
                hasPermission(it)
            }
        }

    companion object {

        private val CONTACTS_READ_PERMISSIONS = listOf(
            Manifest.permission.READ_CONTACTS
        )

        private val AUDIO_RECORDING_PERMISSIONS = listOf(
            Manifest.permission.RECORD_AUDIO
        )

        private val WIFI_NEARBY_PERMISSIONS =
            // On API < 33, we require location permission
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            } else {
                // On API >= 33, we can use the new NEARBY_WIFI_DEVICES permission
                listOf(
                    Manifest.permission.NEARBY_WIFI_DEVICES,
                )
            }
    }
}
