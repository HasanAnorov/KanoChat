package com.ierusalem.androchat.features_tcp.server.permission

import androidx.annotation.CheckResult

interface PermissionGuard {

  @get:CheckResult val requiredPermissions: List<String>
  @get:CheckResult val requiredPermissionsForContacts: List<String>

  @CheckResult suspend fun canCreateNetwork(): Boolean
  @CheckResult suspend fun canAccessContacts(): Boolean

}
