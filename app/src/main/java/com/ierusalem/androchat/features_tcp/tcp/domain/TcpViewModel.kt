package com.ierusalem.androchat.features_tcp.tcp.domain

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.server.IP_ADDRESS_REGEX
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiNetworkEvent
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenNavigation
import com.ierusalem.androchat.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.ui.navigation.emitNavigation
import com.ierusalem.androchat.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

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

            is TcpScreenEvents.SendMessage -> {
                _state.update {
                    val currentTime = Calendar.getInstance().time.toString()
                    val username =
                        if (state.value.isOwner == OwnerStatusState.Owner) "Owner" else "Guest"
                    val newMessages = state.value.messages.toMutableList().apply {
                        add(Message(event.message, currentTime, username))
                    }
                    it.copy(
                        messages = newMessages
                    )
                }
            }

            is TcpScreenEvents.OnPortNumberChanged -> {
                _state.update {
                    it.copy(
                        isValidPortNumber = isValidPortNumber(event.portNumber),
                        portNumber = event.portNumber
                    )
                }
            }

            //todo handle wi fi events globally, like shutdown server and other stuffs
            TcpScreenEvents.DiscoverWifiClick -> {
                if (!state.value.isWifiOn) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.WifiNotEnabled))
                    return
                }
                when (state.value.wifiDiscoveryStatus) {
                    WifiDiscoveryStatus.Idle -> {
                        emitNavigation(TcpScreenNavigation.OnDiscoverWifiClick)
                        updateWifiDiscoveryStatus(WifiDiscoveryStatus.Discovering)
                    }

                    WifiDiscoveryStatus.Discovering -> {
                        emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.AlreadyDiscoveringWifi))
                    }

                    WifiDiscoveryStatus.Failure -> {
                        emitNavigation(TcpScreenNavigation.OnDiscoverWifiClick)
                        updateWifiDiscoveryStatus(WifiDiscoveryStatus.Discovering)
                    }
                }
            }

            TcpScreenEvents.OnNavIconClick -> {
                emitNavigation(TcpScreenNavigation.OnNavIconClick)
            }

            TcpScreenEvents.OnSettingIconClick -> {
                emitNavigation(TcpScreenNavigation.OnSettingsClick)
            }

            TcpScreenEvents.CreateServerClick -> {
                if (!state.value.isValidPortNumber) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }
                if (state.value.groupOwnerAddress == null || !IP_ADDRESS_REGEX.matches(state.value.groupOwnerAddress!!)) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidHostAddress))
                    return
                }
                when (state.value.hotspotTitleStatus) {
                    ServerStatus.Idle -> {
                        emitNavigation(
                            TcpScreenNavigation.OnCreateServerClick(
                                serverIpAddress = state.value.groupOwnerAddress!!,
                                portNumber = state.value.portNumber.toInt()
                            )
                        )
                        updateHotspotTitleStatus(ServerStatus.Creating)
                    }

                    ServerStatus.Creating -> {
                        //just ignore action
                        Log.d("ahi3646", "handleEvents: creating server ")
                    }

                    ServerStatus.Created -> {
                        emitNavigation(TcpScreenNavigation.OnCloseServerClick)
                        Log.d("ahi3646", "handleEvents: created server ")
                    }
                }
            }

            TcpScreenEvents.ConnectToServerClick -> {
                if (!state.value.isValidPortNumber) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }
                if (state.value.groupOwnerAddress == null || !IP_ADDRESS_REGEX.matches(state.value.groupOwnerAddress!!)) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidHostAddress))
                    return
                }
                emitNavigation(
                    TcpScreenNavigation.OnConnectToServerClick(
                        serverIpAddress = state.value.groupOwnerAddress!!,
                        portNumber = state.value.portNumber.toInt()
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

    fun updateConnectionsCount(){
        _state.update {
            it.copy(
                connectionsCount = state.value.connectionsCount +1
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

    val portNumber: String = "9002",
    val isValidPortNumber: Boolean = isValidPortNumber(portNumber),
    val hotspotTitleStatus: ServerStatus = ServerStatus.Idle,

    //wifi p2p state
    val wifiDiscoveryStatus: WifiDiscoveryStatus = WifiDiscoveryStatus.Idle,

    //status
    val connectionStatus: ConnectionStatus = ConnectionStatus.Idle,
    val isWifiOn: Boolean = false,
    val isOwner: OwnerStatusState = OwnerStatusState.Idle,
    val groupOwnerAddress: String? = "127.0.0.1",

    //wifi peers list
    val availableWifiNetworks: List<WifiP2pDevice> = emptyList(),

    //connections
    val connectedWifiNetworks: List<WifiP2pDevice> = emptyList(),

    //chat room
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val connectionsCount: Int = 1
)

enum class TcpScreenErrors(val errorMessage: Int) {
    WifiNotEnabled(R.string.wifi_should_be_enabled_to_perform_this_action),
    AlreadyDiscoveringWifi(R.string.already_discovering_wifi_networks),
    InvalidPortNumber(R.string.try_to_use_another_port_number_current_port_is_already_in_use_or_invalid),
    InvalidHostAddress(R.string.try_to_reconnect_to_the_server_again_current_address_is_invalid),
    FailedToConnectToWifiDevice(R.string.couldn_t_connect_to_choosen_wifi_device),
}

fun isValidPortNumber(portNumber: String): Boolean {
    return portNumber.isNotEmpty() && portNumber.toInt() in Constants.MAX_PORT_NUMBER downTo Constants.MIN_PORT_NUMBER
}

enum class ConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.not_running),
    Running(R.string.waiting_for_connection),
    Connected(R.string.connection_connected),
    Disconnected(R.string.not_connected)
}

enum class OwnerStatusState(@StringRes val status: Int) {
    Idle(R.string.waiting_for_connection),
    Owner(R.string.owner),
    Client(R.string.client)
}

enum class WifiDiscoveryStatus(@StringRes val res: Int, @DrawableRes val icon: Int) {
    Idle(R.string.discover_wifi, R.drawable.wifi),
    Discovering(R.string.discovering_wifi, R.drawable.wifi),
    Failure(R.string.discovering_not_started, R.drawable.error_prompt)
}


enum class ServerStatus(@StringRes val status: Int) {
    Idle(R.string.create_a_server),
    Creating(R.string.creating_a_server),
    Created(R.string.server_created_waiting_for_clients)
}
