package com.ierusalem.androchat.features_tcp.tcp.domain

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.core.utils.isValidHotspotName
import com.ierusalem.androchat.core.utils.isValidIpAddress
import com.ierusalem.androchat.core.utils.isValidPortNumber
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.features.auth.register.domain.model.FileState
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuard
import com.ierusalem.androchat.features_tcp.server.wifidirect.WiFiNetworkEvent
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ClientConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ContactItem
import com.ierusalem.androchat.features_tcp.tcp.domain.state.GeneralConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.GeneralNetworkingStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.HostConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.HotspotNetworkingStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.LocalOnlyHotspotStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.P2PNetworkingStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenDialogErrors
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenErrors
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpScreenNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class TcpViewModel @Inject constructor(
    private val permissionGuardImpl: PermissionGuard,
    private val dataStorePreferenceRepository: DataStorePreferenceRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val wifiManager: WifiManager,
    private val contentResolver: ContentResolver
) : ViewModel(),
    NavigationEventDelegate<TcpScreenNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<TcpScreenUiState> = MutableStateFlow(TcpScreenUiState())
    val state: StateFlow<TcpScreenUiState> = _state.asStateFlow()

    private var hotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null

    init {
        initializeAuthorMe()
        initializeHotspotName()
        listenWifiConnections()
    }

    fun checkReadContactsPermission() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isReadContactsGranted = permissionGuardImpl.canAccessContacts()
                )
            }
        }
    }

    fun updateShowPermissionRequestState(shouldBeShown: Boolean) {
        _state.update {
            it.copy(
                shouldShowPermissionDialog = shouldBeShown
            )
        }
    }

    private fun initializeAuthorMe() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    authorMe = dataStorePreferenceRepository.getUsername.first()
                )
            }
        }
    }

    private fun initializeHotspotName() {
        viewModelScope.launch {
            val saveHotspotName = dataStorePreferenceRepository.getHotspotName.first()
            _state.update {
                it.copy(
                    isValidHotSpotName = isValidHotspotName(saveHotspotName),
                    hotspotName = saveHotspotName
                )
            }
        }
    }

    private fun listenWifiConnections() {
        connectivityObserver.observeWifiState().onEach { connectivityStatus ->
            when (connectivityStatus) {
                ConnectivityObserver.Status.Available -> {
                    log("wifi is connected")
                    updateConnectedWifiAddress(connectivityObserver.getWifiServerIpAddress())
                }

                ConnectivityObserver.Status.Loosing -> {
                    log("wifi is loosing")
                    updateConnectedWifiAddress("Not Connected")
                }

                ConnectivityObserver.Status.Lost -> {
                    log("wifi is disconnected")
                    updateConnectedWifiAddress("Not Connected")
                }

                ConnectivityObserver.Status.Unavailable -> {
                    log("wifi is unavailable")
                    updateConnectedWifiAddress("Not Connected")
                }
            }
        }.launchIn(viewModelScope)
    }

    @SuppressLint("Range")
    private fun readContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = mutableListOf<ContactItem>()
            val cursor: Cursor = contentResolver.query(
                Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    Phone.NUMBER,
                    ContactsContract.RawContacts.ACCOUNT_TYPE
                ),
                ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ",
                null, null
            )!!
            while (cursor.moveToNext()) {
                if (cursor.getColumnIndex(Phone.DISPLAY_NAME) != -1 && cursor.getColumnIndex(
                        Phone.NUMBER
                    ) != -1
                ) {
                    val name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME))
                    val phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER))
                    contacts.add(
                        ContactItem(
                            contactName = name, phoneNumber = phoneNumber, isSelected = false
                        )
                    )
                }
            }
            cursor.close()
            loadContacts(contacts)
        }
    }

    private fun loadContacts(contacts: List<ContactItem>) {
        _state.update {
            it.copy(
                contacts = Resource.Success(contacts)
            )
        }
    }

    private fun updateConnectedWifiAddress(address: String) {
        _state.update {
            it.copy(
                isValidConnectedWifiAddress = isValidIpAddress(address),
                connectedWifiAddress = address
            )
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

            is WiFiNetworkEvent.UpdateClientAddress -> {
                _state.update {
                    it.copy(
                        connectedWifiAddress = networkEvent.clientAddress
                    )
                }
            }

            WiFiNetworkEvent.ThisDeviceChanged -> {
                /**
                 * Broadcast intent action indicating that this device details have changed.
                 * An extra EXTRA_WIFI_P2P_DEVICE provides this device details
                 * */
                //unhandled event
            }

            is WiFiNetworkEvent.ConnectionStatusChanged -> {
                _state.update {
                    it.copy(
                        generalConnectionStatus = networkEvent.status
                    )
                }
            }

            is WiFiNetworkEvent.UpdateGroupOwnerAddress -> {
                _state.update {
                    it.copy(
                        groupOwnerAddress = networkEvent.groupOwnerAddress,
                        isValidGroupOwnerAddress = isValidIpAddress(networkEvent.groupOwnerAddress)
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
        when (state.value.generalNetworkingStatus) {
            GeneralNetworkingStatus.Idle -> {
                //ignore case, nothing has done
            }

            GeneralNetworkingStatus.P2PDiscovery -> {
                emitNavigation(TcpScreenNavigation.WifiDisabledCase)
                updateP2PDiscoveryStatus(P2PNetworkingStatus.Idle)
            }

            GeneralNetworkingStatus.HotspotDiscovery -> {
                emitNavigation(TcpScreenNavigation.WifiDisabledCase)
                updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Idle)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun handleEvents(event: TcpScreenEvents) {
        when (event) {
            is TcpScreenEvents.OnConnectToWifiClick -> {
                emitNavigation(TcpScreenNavigation.OnConnectToWifiClick(event.wifiDevice))
            }

            is TcpScreenEvents.OnContactItemClick -> {
                emitNavigation(TcpScreenNavigation.OnContactItemClick(event.message))
            }

            TcpScreenEvents.ShowFileChooserClick -> {
                emitNavigation(TcpScreenNavigation.ShowFileChooserClick)
            }

            is TcpScreenEvents.OnFileItemClick -> {
                emitNavigation(TcpScreenNavigation.OnFileItemClick(event.message))
            }

            is TcpScreenEvents.UpdateBottomSheetState -> {
                _state.update {
                    it.copy(
                        showBottomSheet = event.shouldBeShown
                    )
                }
            }

            TcpScreenEvents.ReadContacts -> {
                readContacts()
            }

            is TcpScreenEvents.HandlePickingMultipleMedia -> {
                emitNavigation(TcpScreenNavigation.HandlePickingMultipleMedia(event.medias))
            }

            TcpScreenEvents.OnNavIconClick -> {
                emitNavigation(TcpScreenNavigation.OnNavIconClick)
            }

            TcpScreenEvents.OnSettingIconClick -> {
                emitNavigation(TcpScreenNavigation.OnSettingsClick)
            }

            is TcpScreenEvents.OnDialogErrorOccurred -> {
                updateHasErrorOccurredDialog(event.error)
            }

            TcpScreenEvents.ReadContactsRequest -> {
                emitNavigation(TcpScreenNavigation.OnReadContactsRequest)
            }

            is TcpScreenEvents.ReadContactPermissionChanged -> {
                if (!event.isGranted) {
                    updateShowPermissionRequestState(true)
                }
                _state.update {
                    it.copy(
                        isReadContactsGranted = event.isGranted
                    )
                }
            }

            TcpScreenEvents.DiscoverLocalOnlyHotSpotClick -> {
                _state.update {
                    it.copy(
                        localOnlyHotspotStatus = LocalOnlyHotspotStatus.LaunchingHotspot
                    )
                }
                //todo add else case later
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    wifiManager.startLocalOnlyHotspot(
                        object : LocalOnlyHotspotCallback() {
                            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                                super.onStarted(reservation)
                                hotspotReservation = reservation
                                val config: WifiConfiguration? = reservation?.wifiConfiguration

                                log("SSID: ${config?.SSID}, Password: ${config?.preSharedKey}")
                                log("HttpProxy: ${config?.httpProxy}  HiddenSSID: ${config?.hiddenSSID}")
                                log("Local Only Hotspot Started".uppercase())
                                val ip = connectivityObserver.getWifiServerIpAddress()
                                log("wifi ip local-only is : $ip")

                                handleNetworkEvents(WiFiNetworkEvent.UpdateGroupOwnerAddress(ip))
                                _state.update {
                                    it.copy(
                                        localOnlyHotspotStatus = LocalOnlyHotspotStatus.HotspotRunning
                                    )
                                }
                            }

                            override fun onStopped() {
                                super.onStopped()
                                log("Local Only Hotspot Stopped".uppercase())
                                _state.update {
                                    it.copy(
                                        localOnlyHotspotStatus = LocalOnlyHotspotStatus.Idle
                                    )
                                }
                            }

                            override fun onFailed(reason: Int) {
                                super.onFailed(reason)
                                log("Local Only Hotspot Failed".uppercase())
                                _state.update {
                                    it.copy(
                                        localOnlyHotspotStatus = LocalOnlyHotspotStatus.Failure
                                    )
                                }
                            }
                        },
                        null
                    )
                }
            }

            TcpScreenEvents.DiscoverHotSpotClick -> {
                showWifiErrorIfNotEnabled()

                when (state.value.hotspotNetworkingStatus) {
                    HotspotNetworkingStatus.Idle -> {
                        if (showErrorIfOtherNetworkingIsRunning(GeneralNetworkingStatus.HotspotDiscovery)) {
                            return
                        }
                        emitNavigation(TcpScreenNavigation.OnStartHotspotNetworking)
                    }

                    HotspotNetworkingStatus.LaunchingHotspot -> {
                        //just ignore action or implement relaunching feature
                    }

                    HotspotNetworkingStatus.HotspotRunning -> {
                        emitNavigation(TcpScreenNavigation.OnStopHotspotNetworking)
                    }

                    HotspotNetworkingStatus.Failure -> {
                        if (showErrorIfOtherNetworkingIsRunning(GeneralNetworkingStatus.HotspotDiscovery)) {
                            return
                        }
                        emitNavigation(TcpScreenNavigation.OnStartHotspotNetworking)
                    }
                }
            }

            is TcpScreenEvents.SendMessageRequest -> {
                val currentTime = Calendar.getInstance().time.toString()
                val username = state.value.authorMe
                val message = Message.TextMessage(
                    username = username,
                    message = event.message,
                    formattedTime = currentTime,
                    isFromYou = true
                )

                //todo - think about this later
                if (state.value.connectionsCount < 1) {
                    updateHasErrorOccurredDialog(TcpScreenDialogErrors.PeerNotConnected)
                    return
                }
                when (state.value.generalConnectionStatus) {
                    GeneralConnectionStatus.Idle -> {
                        //Establish connection to send message
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
                    }

                    GeneralConnectionStatus.ConnectedAsClient -> {
                        emitNavigation(TcpScreenNavigation.SendClientMessage(message))
                    }

                    GeneralConnectionStatus.ConnectedAsHost -> {
                        emitNavigation(TcpScreenNavigation.SendHostMessage(message))
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

            TcpScreenEvents.DiscoverP2PClick -> {
                showWifiErrorIfNotEnabled()

                when (state.value.p2pNetworkingStatus) {
                    P2PNetworkingStatus.Idle -> {
                        if (showErrorIfOtherNetworkingIsRunning(GeneralNetworkingStatus.P2PDiscovery)) {
                            return
                        }
                        emitNavigation(TcpScreenNavigation.OnDiscoverP2PClick)
                    }

                    P2PNetworkingStatus.Discovering -> {
                        emitNavigation(TcpScreenNavigation.OnStopP2PDiscovery)
                    }

                    P2PNetworkingStatus.Failure -> {
                        emitNavigation(TcpScreenNavigation.OnDiscoverP2PClick)
                        updateP2PDiscoveryStatus(P2PNetworkingStatus.Discovering)
                    }
                }
            }

            TcpScreenEvents.CreateServerClick -> {
                showWifiErrorIfNotEnabled()

                if (!state.value.isValidPortNumber) {
                    Log.d("ahi3646", "handleEvents: invalid port number ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }
                //CLARIFY WHY WE NEED GROUP ADDRESS
//                if (!state.value.isValidGroupOwnerAddress) {
//                    Log.d(
//                        "ahi3646",
//                        "handleEvents: invalid ip address - ${state.value.groupOwnerAddress} "
//                    )
//                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidHostAddress))
//                    return
//                }

                //this when loop determines the state of wi fi connection
                when (state.value.hostConnectionStatus) {
                    HostConnectionStatus.Idle -> {
                        emitNavigation(
                            TcpScreenNavigation.OnCreateServerClick(
                                portNumber = state.value.portNumber.toInt()
                            )
                        )
                    }

                    HostConnectionStatus.Creating -> {
                        //creating server ignore for now
                    }

                    HostConnectionStatus.Created -> {
                        //request for stop
                    }

                    HostConnectionStatus.Failure -> {
                        emitNavigation(
                            TcpScreenNavigation.OnCreateServerClick(
                                portNumber = state.value.portNumber.toInt()
                            )
                        )
                    }
                }
            }

            TcpScreenEvents.ConnectToServerClick -> {

                showWifiErrorIfNotEnabled()

                if (!state.value.isValidPortNumber) {
                    Log.d("ahi3646", "handleEvents: invalid port number ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }
                if (!state.value.isValidConnectedWifiAddress) {
                    Log.d(
                        "ahi3646",
                        "handleEvents: invalid ip address - ${state.value.connectedWifiAddress} "
                    )
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidWiFiServerIpAddress))
                    return
                }

                when (state.value.clientConnectionStatus) {
                    ClientConnectionStatus.Idle -> {
                        emitNavigation(
                            TcpScreenNavigation.OnConnectToServerClick(
                                serverIpAddress = state.value.connectedWifiAddress,
                                portNumber = state.value.portNumber.toInt()
                            )
                        )
                    }

                    ClientConnectionStatus.Connecting -> {
                        log("client is connecting")
                    }

                    ClientConnectionStatus.Connected -> {
                        log("client is connected")
                    }

                    ClientConnectionStatus.Failure -> {
                        log("client is failure")
                        emitNavigation(
                            TcpScreenNavigation.OnConnectToServerClick(
                                serverIpAddress = state.value.connectedWifiAddress,
                                portNumber = state.value.portNumber.toInt()
                            )
                        )
                    }
                }
            }

            is TcpScreenEvents.OnHotspotNameChanged -> {
                viewModelScope.launch {
                    dataStorePreferenceRepository.setHotSpotName(event.hotspotName)
                }
                _state.update {
                    it.copy(
                        isValidHotSpotName = isValidHotspotName(event.hotspotName),
                        hotspotName = event.hotspotName.trim()
                    )
                }
            }
        }
    }

    fun updateClientConnectionStatus(status: ClientConnectionStatus) {
        when (status) {
            ClientConnectionStatus.Idle -> {
                _state.update {
                    it.copy(
                        clientConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }

            ClientConnectionStatus.Connecting,
            ClientConnectionStatus.Connected -> {
                _state.update {
                    it.copy(
                        clientConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.ConnectedAsClient
                    )
                }
            }

            ClientConnectionStatus.Failure -> {
                _state.update {
                    it.copy(
                        clientConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }
        }
    }

    fun updateHostConnectionStatus(status: HostConnectionStatus) {
        when (status) {
            HostConnectionStatus.Idle -> {
                _state.update {
                    it.copy(
                        hostConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }

            HostConnectionStatus.Creating,
            HostConnectionStatus.Created -> {
                _state.update {
                    it.copy(
                        hostConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.ConnectedAsHost
                    )
                }
            }

            HostConnectionStatus.Failure -> {
                _state.update {
                    it.copy(
                        hostConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }
        }
    }

    fun updateP2PDiscoveryStatus(status: P2PNetworkingStatus) {
        when (status) {
            P2PNetworkingStatus.Idle -> {
                _state.update {
                    it.copy(
                        p2pNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.Idle
                    )
                }
            }

            P2PNetworkingStatus.Discovering -> {
                _state.update {
                    it.copy(
                        p2pNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.P2PDiscovery
                    )
                }
            }

            P2PNetworkingStatus.Failure -> {
                _state.update {
                    it.copy(
                        p2pNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.Idle
                    )
                }
            }
        }
    }

    fun clearPeersList() {
        _state.update {
            it.copy(
                availableWifiNetworks = emptyList()
            )
        }
    }

    fun updateHotspotDiscoveryStatus(status: HotspotNetworkingStatus) {
        when (status) {
            HotspotNetworkingStatus.Idle -> {
                _state.update {
                    it.copy(
                        hotspotNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.Idle
                    )
                }
            }

            HotspotNetworkingStatus.LaunchingHotspot,
            HotspotNetworkingStatus.HotspotRunning -> {
                _state.update {
                    it.copy(
                        hotspotNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.HotspotDiscovery,
                    )
                }
            }

            HotspotNetworkingStatus.Failure -> {
                _state.update {
                    it.copy(
                        hotspotNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.Idle
                    )
                }
            }
        }
    }

    private fun showWifiErrorIfNotEnabled() {
        if (!state.value.isWifiOn) {
            emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.WifiNotEnabled))
            return
        }
    }

    private fun showErrorIfOtherNetworkingIsRunning(launchingNetworkStatus: GeneralNetworkingStatus): Boolean {
        return when (launchingNetworkStatus) {
            GeneralNetworkingStatus.Idle -> {
                //ready to launch any networking, just ignore
                false
            }

            GeneralNetworkingStatus.P2PDiscovery -> {
                when (state.value.hotspotNetworkingStatus) {
                    HotspotNetworkingStatus.Idle,
                    HotspotNetworkingStatus.Failure -> {
                        //ignore case
                        false
                    }

                    HotspotNetworkingStatus.HotspotRunning,
                    HotspotNetworkingStatus.LaunchingHotspot -> {
                        //show error here
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.AlreadyHotspotNetworkingRunning)
                        return true
                    }
                }
            }

            GeneralNetworkingStatus.HotspotDiscovery -> {
                when (state.value.p2pNetworkingStatus) {
                    P2PNetworkingStatus.Idle,
                    P2PNetworkingStatus.Failure -> {
                        //ignore case
                        false
                    }

                    P2PNetworkingStatus.Discovering -> {
                        //show error here
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.AlreadyP2PNetworkingRunning)
                        return true
                    }
                }
            }
        }
    }

    fun updateHasErrorOccurredDialog(dialog: TcpScreenDialogErrors?) {
        _state.update {
            it.copy(
                hasDialogErrorOccurred = dialog
            )
        }
    }

    fun updatePercentageOfReceivingFile(message: Message, fileState: FileState) {

        val messages = state.value.messages
        val targetMessage = messages
            .findLast { it.username == message.username && it is Message.FileMessage }
        if (targetMessage == null) return
        val updatedMessage = updateFileState(targetMessage as Message.FileMessage, fileState)
        val newMessages = state.value.messages.toMutableList().apply {
            set(messages.indexOf(targetMessage), updatedMessage)
        }
        _state.update {
            it.copy(
                messages = newMessages
            )
        }

    }

    private fun updateFileState(
        fileMessage: Message.FileMessage,
        newState: FileState
    ): Message.FileMessage {
        return fileMessage.copy(fileState = newState)
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

    fun handleAvailableWifiListChange(peers: List<WifiP2pDevice>) {
        _state.update {
            it.copy(
                availableWifiNetworks = peers
            )
        }
    }

}
