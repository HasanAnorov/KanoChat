package com.ierusalem.androchat.features_tcp.server.wifidirect

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.ierusalem.androchat.features_tcp.tcp.domain.state.GeneralConnectionStatus

@Immutable
sealed interface WiFiNetworkEvent {
    data class UpdateGroupOwnerAddress(val groupOwnerAddress: String): WiFiNetworkEvent
    data class UpdateClientAddress(val clientAddress: String): WiFiNetworkEvent
    data class WifiStateChanged(val isWifiOn: Boolean) : WiFiNetworkEvent
    data object ThisDeviceChanged : WiFiNetworkEvent
    data object DiscoveryChanged : WiFiNetworkEvent
    data class ConnectionStatusChanged(val status: GeneralConnectionStatus) : WiFiNetworkEvent
}
