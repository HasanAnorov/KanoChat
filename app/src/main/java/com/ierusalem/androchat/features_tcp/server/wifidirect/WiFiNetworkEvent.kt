/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ierusalem.androchat.features_tcp.server.wifidirect

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.ierusalem.androchat.features_tcp.tcp.domain.ConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.OwnerStatusState

@Stable
@Immutable
sealed interface WiFiNetworkEvent {
    data class UpdateGroupOwnerAddress(val groupOwnerAddress: String): WiFiNetworkEvent
    data class ConnectedAsWhat(val isOwner:OwnerStatusState): WiFiNetworkEvent
    data class WifiStateChanged(val isWifiOn: Boolean) : WiFiNetworkEvent
    data object ThisDeviceChanged : WiFiNetworkEvent
    data object DiscoveryChanged : WiFiNetworkEvent
    data class ConnectionStatusChanged(val status: ConnectionStatus) : WiFiNetworkEvent
}
