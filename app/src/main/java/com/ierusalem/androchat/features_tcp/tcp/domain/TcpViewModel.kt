package com.ierusalem.androchat.features_tcp.tcp.domain

import android.net.wifi.p2p.WifiP2pDevice
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.R
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


    private fun toggleWifiClick(isWifiOn: Boolean) {
        _state.update {
            it.copy(
                isWifiOn = isWifiOn
            )
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

            is TcpScreenEvents.OnWifiStateChanged -> {
                toggleWifiClick(event.isWifiEnabled)
            }

            TcpScreenEvents.OnNavIconClick -> {
                emitNavigation(TcpScreenNavigation.OnNavIconClick)
            }

            TcpScreenEvents.CreateWifiClick -> {
                emitNavigation(TcpScreenNavigation.OnCreateWiFiClick)
            }

            TcpScreenEvents.OnSettingIconClick -> {
                emitNavigation(TcpScreenNavigation.OnSettingsClick)
            }

            TcpScreenEvents.ConnectToServerClick -> {
                when (state.value.clientTitleStatus) {
                    ClientStatus.Idle -> {
                        emitNavigation(TcpScreenNavigation.OnConnectToServerClick)
                    }

                    ClientStatus.Connecting -> {
                        //just ignore action
                    }

                    ClientStatus.Connected -> {
                        emitNavigation(TcpScreenNavigation.OnDisconnectServerClick)
                    }
                }
            }

            TcpScreenEvents.OpenHotspotClick -> {
                when (state.value.hotspotTitleStatus) {
                    ServerStatus.Idle -> {
                        if ((state.value.portNumber in 65535 downTo 1024) && state.value.hotspotPassword.length > 7) {
                            emitNavigation(
                                TcpScreenNavigation.OnCreateServerClick(
                                    hotspotName = state.value.hotspotName,
                                    hotspotPassword = state.value.hotspotPassword,
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
        }
    }

    fun updateHotspotTitleStatus(status: ServerStatus) {
        _state.update {
            it.copy(
                hotspotTitleStatus = status
            )
        }
    }

    fun updateClientTitleStatus(status: ClientStatus) {
        _state.update {
            it.copy(
                clientTitleStatus = status
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
    //server side state
    val hotspotName: String = "",
    val hotspotPassword: String = generateRandomPassword(),
    val portNumber: Int = 9002,

    val hotspotTitleStatus: ServerStatus = ServerStatus.Idle,
    val clientTitleStatus: ClientStatus = ClientStatus.Idle,
    val wifiDiscoveryStatus: WifiDiscoveryStatus = WifiDiscoveryStatus.Idle,

    val isWifiOn: Boolean = false,
    val availableWifiNetworks: List<WifiP2pDevice> = emptyList(),
)

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

enum class ClientStatus(@StringRes val status: Int) {
    Idle(R.string.connect_to_server),
    Connecting(R.string.connecting_to_server),
    Connected(R.string.connected_to_a_server)
}