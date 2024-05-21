package com.ierusalem.androchat.features_tcp.server.broadcast

import androidx.annotation.CheckResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface BroadcastObserver {

  @CheckResult suspend fun listenNetworkEvents(scope: CoroutineScope): Flow<BroadcastEvent>
}
