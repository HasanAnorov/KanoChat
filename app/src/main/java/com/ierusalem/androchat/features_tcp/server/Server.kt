package com.ierusalem.androchat.features_tcp.server

import androidx.annotation.CheckResult
import com.ierusalem.androchat.features_tcp.server.status.RunningStatus
import kotlinx.coroutines.flow.Flow

interface Server {

  @CheckResult fun getCurrentStatus(): RunningStatus

  @CheckResult fun onStatusChanged(): Flow<RunningStatus>
}
