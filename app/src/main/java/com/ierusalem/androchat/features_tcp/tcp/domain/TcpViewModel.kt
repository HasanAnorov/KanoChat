package com.ierusalem.androchat.features_tcp.tcp.domain

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.ierusalem.androchat.utils.DataStorePreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TcpViewModel @Inject constructor(
    private val dataStorePreferenceRepository: DataStorePreferenceRepository
) : ViewModel(),
    NavigationEventDelegate<TcpScreenNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<TcpScreenUiState> = MutableStateFlow(TcpScreenUiState())
    val state: StateFlow<TcpScreenUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    authorMe = dataStorePreferenceRepository.getUsername.first()
                )
            }
        }
    }

    fun handleNetworkEvents(networkEvent: WiFiNetworkEvent) {
        when (networkEvent) {
            WiFiNetworkEvent.DiscoveryChanged -> {
                /***
                 * Broadcast intent action indicating that peer discovery
                 * has either started or stopped. One extra EXTRA_DISCOVERY_STATE indicates
                 * whether discovery has started or stopped.
                 * */
                //unhandled event
            }

            WiFiNetworkEvent.ThisDeviceChanged -> {
                /**
                 * Broadcast intent action indicating that this device details have changed.
                 * An extra EXTRA_WIFI_P2P_DEVICE provides this device details
                 * */
                //unhandled event
            }

            is WiFiNetworkEvent.ConnectedAsWhat -> {
                _state.update {
                    it.copy(
                        isOwner = networkEvent.isOwner
                    )
                }
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
                if (!networkEvent.isWifiOn) {
                    handleWifiDisableCase()
                }
            }
        }
    }

    private fun handleWifiDisableCase() {
        //emit event for closing sockets
        emitNavigation(TcpScreenNavigation.WifiDisabledCase(state.value.isOwner))

        updateServerTitleStatus(HostConnectionStatus.Idle)
        updateClientTitleStatus(ClientConnectionStatus.Idle)
        updateWifiDiscoveryStatus(WifiDiscoveryStatus.Idle)

        //decrease connections count
        updateConnectionsCount(false)

    }

    fun handleEvents(event: TcpScreenEvents) {
        when (event) {
            is TcpScreenEvents.OnConnectToWifiClick -> {
                emitNavigation(TcpScreenNavigation.OnConnectToWifiClick(event.wifiDevice))
            }

            TcpScreenEvents.OnNavIconClick -> {
                emitNavigation(TcpScreenNavigation.OnNavIconClick)
            }

            TcpScreenEvents.OnSettingIconClick -> {
                emitNavigation(TcpScreenNavigation.OnSettingsClick)
            }

            is TcpScreenEvents.UpdateClientStatus -> {
                updateClientTitleStatus(event.status)
            }

            is TcpScreenEvents.UpdateServerStatus -> {
                updateServerTitleStatus(event.status)
            }

            is TcpScreenEvents.OnDialogErrorOccurred -> {
                updateHasErrorOccurredDialog(event.error)
            }

            is TcpScreenEvents.InsertMessage -> {
                insertMessage(event.message)
            }

            is TcpScreenEvents.SendMessageRequest -> {
                val currentTime = Calendar.getInstance().time.toString()
                val username = state.value.authorMe
                val message = Message(event.message, currentTime, username)
                when (state.value.isOwner) {
                    OwnerStatusState.Idle -> {
                        //ignore sending message
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
                    }

                    OwnerStatusState.Client -> {
                        when (state.value.clientConnectionStatus) {
                            ClientConnectionStatus.Idle -> {
                                //Has not connected to a server
                                //ignore sending message
                            }

                            ClientConnectionStatus.Creating -> {
                                //connecting to a server
                                //currently it's not possible to send messages to a server
                            }

                            ClientConnectionStatus.Created -> {
                                emitNavigation(TcpScreenNavigation.SendClientMessage(message))
                            }
                        }
                    }

                    OwnerStatusState.Owner -> {
                        when (state.value.hostConnectionStatus) {
                            HostConnectionStatus.Idle -> {
                                //Has not created a server
                                //ignore sending message
                            }

                            HostConnectionStatus.Creating -> {
                                //creating a server
                                //currently it's not possible to send messages
                            }

                            HostConnectionStatus.Created -> {
                                emitNavigation(TcpScreenNavigation.SendHostMessage(message))
                            }
                        }
                    }

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

            TcpScreenEvents.CreateServerClick -> {
                if (!state.value.isWifiOn) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.WifiNotEnabled))
                    return
                }
                if (!state.value.isValidPortNumber) {
                    Log.d("ahi3646", "handleEvents: invalid port number ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }
                if (state.value.groupOwnerAddress == null || !IP_ADDRESS_REGEX.matches(state.value.groupOwnerAddress!!)) {
                    Log.d("ahi3646", "handleEvents: invalid ip address ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidHostAddress))
                    return
                }

                //this when loop determines the state of wi fi connection
                when (state.value.isOwner) {
                    OwnerStatusState.Idle -> {
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.ServerCreationOrConnectionWithoutWifiConnection)
                    }

                    OwnerStatusState.Client -> {
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.YouAreNotOwner)
                    }

                    OwnerStatusState.Owner -> {
                        //this when loop determines the state of tcp connection
                        when (state.value.hostConnectionStatus) {
                            HostConnectionStatus.Idle -> {
                                emitNavigation(
                                    TcpScreenNavigation.OnCreateServerClick(
                                        portNumber = state.value.portNumber.toInt()
                                    )
                                )
                                updateServerTitleStatus(HostConnectionStatus.Creating)
                            }

                            HostConnectionStatus.Creating -> {
                                //just ignore action
                                Log.d("ahi3646", "handleEvents: creating server ")
                            }

                            HostConnectionStatus.Created -> {
                                //todo request server close here
                                //emitNavigation(TcpScreenNavigation.OnCloseServerClick)
                                Log.d("ahi3646", "handleEvents: created server viewModel")
                            }
                        }
                    }
                }
            }

            TcpScreenEvents.ConnectToServerClick -> {
                if (!state.value.isWifiOn) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.WifiNotEnabled))
                    return
                }
                if (!state.value.isValidPortNumber) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }
                if (state.value.groupOwnerAddress == null || !IP_ADDRESS_REGEX.matches(state.value.groupOwnerAddress!!)) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidHostAddress))
                    return
                }

                //this when loop determines the state of wi fi connection
                when (state.value.isOwner) {
                    OwnerStatusState.Idle -> {
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.ServerCreationOrConnectionWithoutWifiConnection)
                    }

                    OwnerStatusState.Owner -> {
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.YouAreNotClient)
                    }

                    OwnerStatusState.Client -> {
                        //this when loop determines the state of tcp connection
                        when (state.value.clientConnectionStatus) {
                            ClientConnectionStatus.Idle -> {
                                emitNavigation(
                                    TcpScreenNavigation.OnConnectToServerClick(
                                        serverIpAddress = state.value.groupOwnerAddress!!,
                                        portNumber = state.value.portNumber.toInt()
                                    )
                                )
                                updateClientTitleStatus(ClientConnectionStatus.Creating)
                            }

                            ClientConnectionStatus.Creating -> {
                                //just ignore action
                                Log.d("ahi3646", "handleEvents: creating client ")
                            }

                            ClientConnectionStatus.Created -> {
                                //todo request clientSocket close here
                                Log.d("ahi3646", "handleEvents: created client viewModel")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateHasErrorOccurredDialog(dialog: TcpScreenDialogErrors?) {
        _state.update {
            it.copy(
                hasErrorOccurredDialog = dialog
            )
        }
    }

    fun insertMessage(message: Message) {
        val newMessages = state.value.messages.toMutableList().apply {
            add(message)
        }
        _state.update {
            it.copy(
                messages = newMessages
            )
        }
    }

    private fun updateServerTitleStatus(status: HostConnectionStatus) {
        _state.update {
            it.copy(
                hostConnectionStatus = status
            )
        }
    }

    private fun updateClientTitleStatus(status: ClientConnectionStatus) {
        _state.update {
            it.copy(
                clientConnectionStatus = status
            )
        }
    }

    fun updateConnectionsCount(shouldIncrease: Boolean) {
        if (shouldIncrease) {
            _state.update {
                it.copy(
                    connectionsCount = state.value.connectionsCount + 1
                )
            }
        } else {
            _state.update {
                it.copy(
                    connectionsCount = state.value.connectionsCount - 1
                )
            }
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

    val authorMe: String = Constants.UNKNOWN_USER,

    val portNumber: String = "9002",
    val isValidPortNumber: Boolean = isValidPortNumber(portNumber),
    val hostConnectionStatus: HostConnectionStatus = HostConnectionStatus.Idle,
    val clientConnectionStatus: ClientConnectionStatus = ClientConnectionStatus.Idle,

    //wifi p2p state
    val wifiDiscoveryStatus: WifiDiscoveryStatus = WifiDiscoveryStatus.Idle,

    //status
    val connectionStatus: ConnectionStatus = ConnectionStatus.Idle,
    val isWifiOn: Boolean = false,
    val isOwner: OwnerStatusState = OwnerStatusState.Idle,
    val groupOwnerAddress: String? = "No connection",

    //wifi peers list
    val availableWifiNetworks: List<WifiP2pDevice> = emptyList(),

    //connections
    val connectedWifiNetworks: List<WifiP2pDevice> = emptyList(),

    //chat room
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val connectionsCount: Int = 1,

    //error handling
    val hasErrorOccurredDialog: TcpScreenDialogErrors? = null
)

enum class TcpCloseDialogReason(@StringRes val reason: Int, @StringRes val description: Int) {

    //This error case will be removed when "saving existing messages" feature will be implemented
    ExistingMessages(
        R.string.message_are_not_saved,
        R.string.you_have_existing_messages_with_your_partner_and_if_you_close_the_this_window_messages_will_not_be_saved
    ),

    ExistingConnection(
        R.string.the_connection_will_not_be_saved,
        R.string.you_have_established_connection_with_your_partner_if_you_close_this_window_the_connection_will_not_be_saved
    )
}

enum class TcpScreenErrors(@StringRes val errorMessage: Int) {
    WifiNotEnabled(R.string.wifi_should_be_enabled_to_perform_this_action),
    AlreadyDiscoveringWifi(R.string.already_discovering_wifi_networks),
    InvalidPortNumber(R.string.try_to_use_another_port_number_current_port_is_already_in_use_or_invalid),
    InvalidHostAddress(R.string.try_to_reconnect_to_the_server_again_current_address_is_invalid),
    FailedToConnectToWifiDevice(R.string.couldn_t_connect_to_chosen_wifi_device),
}

enum class TcpScreenDialogErrors(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @DrawableRes val icon: Int
) {
    EOException(
        R.string.network_error_occurred,
        R.string.your_network_connection_was_interrupted_check_your_connection_and_try_again,
        R.drawable.wifi_off
    ),
    IOException(
        R.string.network_error_occurred,
        R.string.your_network_connection_was_interrupted_check_your_connection_and_try_again,
        R.drawable.wifi_off
    ),
    UTFDataFormatException(
        R.string.network_error_occurred,
        R.string.incoming_messages_are_not_in_utf_8_format_the_data_do_not_represent_a_valid_modified_utf_8_encoding_of_a_string,
        R.drawable.error_prompt
    ),
    UnknownHostException(
        R.string.invalid_host_ip_address,
        R.string.the_ip_address_of_the_host_could_not_be_determined,
        R.drawable.info
    ),
    YouAreNotOwner(
        R.string.you_are_not_the_owner,
        R.string.you_connected_as_client_thus_you_can_t_create_a_server,
        R.drawable.info
    ),
    YouAreNotClient(
        R.string.you_are_not_the_client,
        R.string.you_connected_as_a_host_for_this_server_thus_you_can_t_be_a_client,
        R.drawable.info
    ),
    ServerCreationOrConnectionWithoutWifiConnection(
        R.string.no_wifi_connection,
        R.string.you_need_to_connect_to_a_wifi_network_to_create_or_connect_to_a_server,
        R.drawable.info
    ),
    EstablishConnectionToSendMessage(
        R.string.no_one_to_chat,
        R.string.could_not_establish_connection_with_your_partner_please_try_to_reconnect_and_try_again,
        R.drawable.info
    )
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


enum class HostConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.create_a_server),
    Creating(R.string.creating_a_server),
    Created(R.string.server_created_waiting_for_clients)
}

enum class ClientConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.connect),
    Creating(R.string.connecting_to_server),
    Created(R.string.connected_to_a_server)
}
