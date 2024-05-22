package com.ierusalem.androchat.features_tcp.tcp.domain

import android.net.wifi.p2p.WifiP2pDevice
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiNetworkEvent
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenNavigation
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.emitNavigation
import com.ierusalem.androchat.utils.generateRandomPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TcpViewModel : ViewModel(),
    NavigationEventDelegate<TcpScreenNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<TcpScreenUiState> = MutableStateFlow(
        TcpScreenUiState()
    )
    val state = _state.asStateFlow()

    fun handleNetworkEvents(networkEvent: WiFiNetworkEvent) {
        when (networkEvent) {
            is WiFiNetworkEvent.ConnectedAsWhat -> {
                _state.update {
                    it.copy(
                        isOwner = networkEvent.isOwner
                    )
                }
            }

            WiFiNetworkEvent.DiscoveryChanged -> {
                //unhandled event
            }

            WiFiNetworkEvent.ThisDeviceChanged -> {
                //unhandled event
            }

            is WiFiNetworkEvent.ConnectionStatusChanged -> {
                _state.update {
                    it.copy(
                        connectionStatus = networkEvent.status
                    )
                }
            }

            is WiFiNetworkEvent.UpdateGroupOwnerAddress -> {
                _state.update {
                    it.copy(
                        groupOwnerAddress = networkEvent.groupOwnerAddress
                    )
                }
            }

            is WiFiNetworkEvent.WifiStateChanged -> {
                _state.update {
                    it.copy(
                        isWifiOn = networkEvent.isWifiOn
                    )
                }
            }
        }
    }

    fun handleEvents(event: TcpScreenEvents) {
        when (event) {
            is TcpScreenEvents.OnConnectToWifiClick -> {
                emitNavigation(TcpScreenNavigation.OnConnectToWifiClick(event.wifiDevice))
            }

            TcpScreenEvents.DiscoverWifiClick -> {
                emitNavigation(TcpScreenNavigation.OnDiscoverWifiClick)
            }

            TcpScreenEvents.OnNavIconClick -> {
                emitNavigation(TcpScreenNavigation.OnNavIconClick)
            }

            TcpScreenEvents.OnSettingIconClick -> {
                emitNavigation(TcpScreenNavigation.OnSettingsClick)
            }

            TcpScreenEvents.CreateServerClick -> {
                when (state.value.hotspotTitleStatus) {
                    ServerStatus.Idle -> {
                        if ((state.value.portNumber in 65535 downTo 1024) && state.value.hotspotPassword.length > 7) {
                            emitNavigation(
                                TcpScreenNavigation.OnCreateServerClick(
                                    serverIpAddress = state.value.groupOwnerAddress ?: "",
                                    portNumber = state.value.portNumber
                                )
                            )
                            updateHotspotTitleStatus(ServerStatus.Creating)
                        } else {
                            //todo show error message
                        }
                    }

                    ServerStatus.Creating -> {
                        //just ignore action
                    }

                    ServerStatus.Created -> {
                        emitNavigation(TcpScreenNavigation.OnCloseServerClick)
                    }
                }
            }
            TcpScreenEvents.ConnectToServerClick -> {
                emitNavigation(
                    TcpScreenNavigation.OnConnectToServerClick(
                        serverIpAddress = state.value.groupOwnerAddress ?: "",
                        portNumber = state.value.portNumber
                    )
                )
            }
        }
    }

    fun updateHotspotTitleStatus(status: ServerStatus) {
        _state.update {
            it.copy(
                hotspotTitleStatus = status
            )
        }
    }

    fun updateWifiDiscoveryStatus(status: WifiDiscoveryStatus) {
        _state.update {
            it.copy(
                wifiDiscoveryStatus = status
            )
        }
    }

    fun handleAvailableWifiListChange(peers: List<WifiP2pDevice>) {
        _state.update {
            it.copy(
                availableWifiNetworks = peers
            )
        }
    }

}

@Immutable
data class TcpScreenUiState(
    //tcp server side state
    val hotspotName: String = "",
    val hotspotPassword: String = generateRandomPassword(),

    val portNumber: Int = 1020,
    val hotspotTitleStatus: ServerStatus = ServerStatus.Idle,

    //wifi p2p state
    val wifiDiscoveryStatus: WifiDiscoveryStatus = WifiDiscoveryStatus.Idle,

    //status
    val connectionStatus: ConnectionStatus = ConnectionStatus.Idle,
    val isWifiOn: Boolean = false,
    val isOwner: OwnerStatusState = OwnerStatusState.Idle,
    val groupOwnerAddress: String? = null,

    //wifi peers list
    val availableWifiNetworks: List<WifiP2pDevice> = emptyList(),
)

enum class ConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.not_running),
    Running(R.string.running),
    Connected(R.string.connection_connected),
    Disconnected(R.string.not_connected)
}

enum class OwnerStatusState(@StringRes val status: Int) {
    Idle(R.string.waiting_for_connection),
    Owner(R.string.owner),
    Client(R.string.client)
}

enum class WifiDiscoveryStatus(@StringRes val res: Int) {
    Idle(R.string.discover_wifi),
    Discovering(R.string.discovering_wifi),
    Failure(R.string.discovering_not_started)
}


enum class ServerStatus(@StringRes val status: Int) {
    Idle(R.string.create_a_server),
    Creating(R.string.creating_a_server),
    Created(R.string.server_created_waiting_for_clients)
}
