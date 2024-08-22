package com.ierusalem.androchat.features_tcp.server.permission

import androidx.annotation.CheckResult

interface PermissionGuard {

  @get:CheckResult val requiredPermissionsForWifi: List<String>
  @get:CheckResult val requiredPermissionsForContacts: List<String>
  @get:CheckResult val requiredPermissionsForRecordingAudio: List<String>
  @get:CheckResult val requiredPermissionsForLocalOnlyHotSpot: List<String>

  @CheckResult suspend fun canCreateNetwork(): Boolean
  @CheckResult suspend fun canCreateLocalOnlyHotSpotNetwork(): Boolean
  @CheckResult suspend fun canAccessContacts(): Boolean
  @CheckResult suspend fun canRecordAudio(): Boolean

}
