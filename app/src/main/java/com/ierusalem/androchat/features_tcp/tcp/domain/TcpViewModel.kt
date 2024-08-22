package com.ierusalem.androchat.features_tcp.tcp.domain

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Build
import android.os.Environment
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.app.BroadcastFrequency
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.core.constants.Constants.getCurrentTime
import com.ierusalem.androchat.core.constants.Constants.getTimeInHours
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.core.utils.UiText
import com.ierusalem.androchat.core.utils.getAudioFileDuration
import com.ierusalem.androchat.core.utils.isValidHotspotName
import com.ierusalem.androchat.core.utils.isValidHotspotPassword
import com.ierusalem.androchat.core.utils.isValidIpAddress
import com.ierusalem.androchat.core.utils.isValidPortNumber
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.voice_message.playback.AndroidAudioPlayer
import com.ierusalem.androchat.core.voice_message.recorder.AndroidAudioRecorder
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
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.dao.MessagesDao
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.AudioState
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TcpViewModel @Inject constructor(
    private val permissionGuardImpl: PermissionGuard,
    private val dataStorePreferenceRepository: DataStorePreferenceRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val wifiManager: WifiManager,
    private val contentResolver: ContentResolver,
    private val messagesDao: MessagesDao,
    private val audioRecorder: AndroidAudioRecorder,
    private val audioPlayer: AndroidAudioPlayer
) : ViewModel(), NavigationEventDelegate<TcpScreenNavigation> by DefaultNavigationEventDelegate() {

    private val _state: MutableStateFlow<TcpScreenUiState> = MutableStateFlow(TcpScreenUiState())
    val state: StateFlow<TcpScreenUiState> = _state.asStateFlow()

    val visiblePermissionDialogQueue = mutableStateListOf<String>()
    val visibleActionDialogQueue = mutableStateListOf<VisibleActionDialogs>()

    private var hotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null

    private val resourceDirectory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}")

    init {
        initializeUserUniqueName()
        initBroadcastFrequency()
        initializeHotspotName()
        initializeHotspotPassword()
        listenWifiConnections()
    }

    private fun initializeUserUniqueName() {
        viewModelScope.launch {
            val savedUniqueUsername = dataStorePreferenceRepository.getUsername.first()
            _state.update {
                it.copy(
                    userUniqueName = savedUniqueUsername
                )
            }
        }
    }

    private fun initBroadcastFrequency() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedBroadcastFrequency =
                dataStorePreferenceRepository.getBroadcastFrequency.first()
            val broadcastFrequency = try {
                BroadcastFrequency.valueOf(savedBroadcastFrequency)
            } catch (e: IllegalArgumentException) {
                BroadcastFrequency.FREQUENCY_2_4_GHZ
            }
            _state.update { settingsState ->
                settingsState.copy(
                    networkBand = broadcastFrequency
                )
            }
        }
    }

    fun dismissPermissionDialog() {
        log("dismissPermissionDialog")
        visiblePermissionDialogQueue.forEach {
            log("visible permission - $it")
        }
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            log("adding permission to dialog queue - $permission")
            visiblePermissionDialogQueue.add(permission)
        }
        when (permission) {
            Manifest.permission.READ_CONTACTS -> {
                _state.update {
                    it.copy(
                        isReadContactsGranted = isGranted
                    )
                }
            }
        }
    }

    private fun updateInitialChatModel(initialChatModel: InitialChatModel) {
        _state.update {
            it.copy(
                peerUserUniqueId = initialChatModel.userUniqueId,
                peerUniqueName = initialChatModel.userUniqueName
            )
        }
    }



    private fun startCollectingPlayTiming(messageId: Long) {
        viewModelScope.launch {
            audioPlayer
                .playTiming
                .distinctUntilChanged()
                .collect { timing ->
                    log("timing - $timing")
                    updatePlayTiming(timing, messageId)
                }
        }
    }

    fun loadChatHistory(initialChatModel: InitialChatModel) {
        updateInitialChatModel(initialChatModel)
        viewModelScope.launch(Dispatchers.IO) {
            messagesDao.getUserMessagesById(initialChatModel.userUniqueId).collect { messages ->
                _state.update { uiState ->
                    messages.forEach {
                        log("message - $it")
                    }
                    uiState.copy(
                        messages = messages.mapNotNull {
                            it.toChatMessage(initialChatModel.userUniqueName)
                        }
                    )
                }
            }
        }
    }

    suspend fun getUniqueDeviceId(): String {
        return dataStorePreferenceRepository.getUniqueDeviceId.first()
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

    private fun initializeHotspotPassword() {
        viewModelScope.launch {
            val savedHotspotPassword = dataStorePreferenceRepository.getHotspotPassword.first()
            _state.update {
                it.copy(
                    isValidHotSpotPassword = isValidHotspotPassword(savedHotspotPassword),
                    hotspotPassword = savedHotspotPassword
                )
            }
        }
    }

    private fun initializeHotspotName() {
        viewModelScope.launch {
            val savedHotspotName = dataStorePreferenceRepository.getHotspotName.first()
            _state.update {
                it.copy(
                    isValidHotSpotName = isValidHotspotName(savedHotspotName),
                    hotspotName = savedHotspotName
                )
            }
        }
    }

    private fun listenWifiConnections() {
        connectivityObserver.observeWifiState().onEach { connectivityStatus ->
            when (connectivityStatus) {
                ConnectivityObserver.Status.Available -> {
                    updateConnectedWifiAddress(connectivityObserver.getWifiServerIpAddress())
                }

                ConnectivityObserver.Status.Loosing -> {
                    updateConnectedWifiAddress("Not Connected")
                }

                ConnectivityObserver.Status.Lost -> {
                    updateConnectedWifiAddress("Not Connected")
                }

                ConnectivityObserver.Status.Unavailable -> {
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
                Phone.CONTENT_URI, arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    Phone.NUMBER,
                    ContactsContract.RawContacts.ACCOUNT_TYPE
                ), ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ", null, null
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
            _state.update {
                it.copy(
                    contacts = Resource.Success(contacts)
                )
            }
        }
    }

    private fun updateConnectedWifiAddress(address: String) {
        _state.update {
            it.copy(
                isValidConnectedWifiAddress = isValidIpAddress(address),
                connectedWifiAddress = address,
                connectedServerAddress = UiText.DynamicString(address)
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

            WiFiNetworkEvent.ThisDeviceChanged -> {
                /**
                 * Broadcast intent action indicating that this device details have changed.
                 * An extra EXTRA_WIFI_P2P_DEVICE provides this device details
                 * */
                //unhandled event
            }

            is WiFiNetworkEvent.UpdateClientAddress -> {
                updateConnectedWifiAddress(address = networkEvent.clientAddress)
            }

            is WiFiNetworkEvent.ConnectionStatusChanged -> {
                log("on general connection status changed 1 - network event - $networkEvent")
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
                        isValidGroupOwnerAddress = isValidIpAddress(networkEvent.groupOwnerAddress),
                        connectedServerAddress = UiText.DynamicString(networkEvent.groupOwnerAddress)
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
            GeneralNetworkingStatus.Idle, GeneralNetworkingStatus.LocalOnlyHotspot -> {
                //ignore case
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

    private lateinit var currentAudioFile: File

    private fun updateIsRecording(isRecording: Boolean) {
        _state.update {
            it.copy(
                isRecording = isRecording
            )
        }
    }

    private fun startRecording() {
        updateIsRecording(true)
        val currentAudioFileName =
            "${Constants.VOICE_MESSAGE_FILE_NAME}${getTimeInHours()}${Constants.AUDIO_EXTENSION}"

        currentAudioFile = File(resourceDirectory, currentAudioFileName).also {
            audioRecorder.startAudio(it)
        }
    }

    private fun finishRecording() {
        updateIsRecording(false)
        audioRecorder.stopAudio()

        val voiceMessageEntity = ChatMessageEntity(
            type = AppMessageType.VOICE,
            formattedTime = getCurrentTime(),
            isFromYou = true,
            userId = state.value.peerUserUniqueId,
            voiceMessageFileName = currentAudioFile.name,
            voiceMessageAudioFileDuration = currentAudioFile.getAudioFileDuration(),
        )

        when (state.value.generalConnectionStatus) {
            GeneralConnectionStatus.Idle -> {
                log("recording voice message on idle connection")
                cancelRecording()
                updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
            }

            GeneralConnectionStatus.ConnectedAsClient -> {
                emitNavigation(TcpScreenNavigation.SendClientMessage(voiceMessageEntity))
            }

            GeneralConnectionStatus.ConnectedAsHost -> {
                emitNavigation(TcpScreenNavigation.SendHostMessage(voiceMessageEntity))
            }
        }
    }

    private fun cancelRecording() {
        updateIsRecording(false)
        audioRecorder.stopAudio()
        if (currentAudioFile.exists()) {
            val isDeleted = currentAudioFile.delete()
            log("is cancelled audio deleted - $isDeleted")
        } else {
            log("file does not exist but cancel record called")
        }
    }

    private fun updateIsPlaying(audioState: AudioState, messageId: Long) {
        // Find the target message
        val targetMessage: ChatMessage.VoiceMessage? = state.value.messages
            .find { it.messageId == messageId && it.messageType == AppMessageType.VOICE } as? ChatMessage.VoiceMessage
        // Check if targetMessage is not null
        targetMessage?.let { message ->
            // Create a copy with the updated isPlaying value
            val updatedMessage = message.copy(audioState = audioState)
            // Create a new list with the updated message
            val newMessages = state.value.messages.map { msg ->
                if (msg.messageId == messageId && msg.messageType == AppMessageType.VOICE) {
                    updatedMessage
                } else if (msg.messageType == AppMessageType.VOICE) {
                    (msg as ChatMessage.VoiceMessage).copy(audioState = AudioState.Idle)
                } else {
                    msg
                }
            }
            // Update the state with the new list of messages
            _state.update { currentState ->
                currentState.copy(
                    messages = newMessages
                )
            }
        }
    }

    private fun updatePlayTiming(timing: Long, messageId: Long) {
        // Find the target message
        val targetMessage: ChatMessage.VoiceMessage? = state.value.messages
            .find { it.messageId == messageId && it.messageType == AppMessageType.VOICE } as? ChatMessage.VoiceMessage
        // Check if targetMessage is not null
        targetMessage?.let {
            val newAudioState = AudioState.Playing(timing)
            // Create a copy with the updated isPlaying value
            val updatedMessage = it.copy(audioState = newAudioState)
            // Get the current list of messages
            val messages = state.value.messages
            // Find the index of the target message
            val targetMessageIndex = messages.indexOf(targetMessage)
            // Create a new list with the updated message
            val newMessages = messages.toMutableList().apply {
                set(targetMessageIndex, updatedMessage)
            }
            // Update the state with the new list of messages
            _state.update { currentState ->
                currentState.copy(
                    messages = newMessages
                )
            }
        }
    }

    private fun playAudioFile(voiceMessage: ChatMessage.VoiceMessage) {
        val audioFile = File(resourceDirectory, voiceMessage.voiceFileName)
        when (voiceMessage.audioState) {
            is AudioState.Playing -> {
                val currentPosition = audioPlayer.pause()
                currentPosition?.let {
                    updateIsPlaying(AudioState.Paused(currentPosition), voiceMessage.messageId)
                }
            }

            is AudioState.Paused -> {
                log("starting resume on ${voiceMessage.audioState.currentPosition}")
                startCollectingPlayTiming(voiceMessage.messageId)
                audioPlayer.resumeAudioFile(audioFile, voiceMessage.audioState.currentPosition) {
                    log("on finished in vm 1")
                    updateIsPlaying(AudioState.Idle, voiceMessage.messageId)
                }
            }

            AudioState.Idle -> {
                audioPlayer.playAudioFile(audioFile) {
                    log("on finished in vm")
                    updateIsPlaying(AudioState.Idle, voiceMessage.messageId)
                }
                updateIsPlaying(AudioState.Playing(0L), voiceMessage.messageId)
                startCollectingPlayTiming(voiceMessage.messageId)
            }
        }
    }

    private fun createLocalOnlyHotspotNetwork() {
        viewModelScope.launch {
            if (permissionGuardImpl.canCreateLocalOnlyHotSpotNetwork()) {
                startLocalOnlyHotspot()
            } else {
                permissionGuardImpl.requiredPermissionsForLocalOnlyHotSpot.forEach {
                    if (!visiblePermissionDialogQueue.contains(it)) {
                        visiblePermissionDialogQueue.add(it)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun startLocalOnlyHotspot() {

        updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.LaunchingLocalOnlyHotspot)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val callback = object : LocalOnlyHotspotCallback() {
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

                    updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.LocalOnlyHotspotRunning)
                }

                override fun onStopped() {
                    super.onStopped()
                    log("Local Only Hotspot Stopped".uppercase())
                    updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.Idle)
                }

                override fun onFailed(reason: Int) {
                    super.onFailed(reason)
                    log("Local Only Hotspot Failed".uppercase())
                    updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.Failure)
                }
            }
            wifiManager.startLocalOnlyHotspot(callback, null)
        } else {
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.LocalOnlyHotspotNotSupported)
            updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.Failure)
        }
    }

    @SuppressLint("NewApi")
    fun handleEvents(event: TcpScreenEvents) {
        when (event) {
            //todo - you did not use the parameter inside on play voice message
            is TcpScreenEvents.OnPlayVoiceMessageClick -> {
                playAudioFile(event.message)
            }

            TcpScreenEvents.RequestRecordAudioPermission -> {
                emitNavigation(TcpScreenNavigation.RequestRecordAudioPermission)
            }

            is TcpScreenEvents.OnPauseVoiceMessageClick -> {
                val currentPosition = audioPlayer.pause()
                currentPosition?.let {
                    updateIsPlaying(AudioState.Paused(currentPosition), event.message.messageId)
                }
                log("paused at - $currentPosition")
            }

            is TcpScreenEvents.OnStopVoiceMessageClick -> {
                updateIsPlaying(AudioState.Idle, event.message.messageId)
                audioPlayer.stop()
            }

            TcpScreenEvents.OnVoiceRecordStart -> {
                startRecording()
            }

            TcpScreenEvents.OnVoiceRecordFinished -> {
                finishRecording()
            }

            TcpScreenEvents.OnVoiceRecordCancelled -> {
                cancelRecording()
            }

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

            TcpScreenEvents.RequestReadContactsPermission -> {
                emitNavigation(TcpScreenNavigation.RequestReadContactsPermission)
            }

            is TcpScreenEvents.OnDialogErrorOccurred -> {
                updateHasErrorOccurredDialog(event.error)
            }

            TcpScreenEvents.DiscoverLocalOnlyHotSpotClick -> {
                when (state.value.localOnlyHotspotNetworkingStatus) {
                    LocalOnlyHotspotStatus.Idle -> {
                        if (hasOtherNetworkingIsRunning()) {
                            return
                        }
                        createLocalOnlyHotspotNetwork()
                    }

                    LocalOnlyHotspotStatus.LaunchingLocalOnlyHotspot -> {
                        //ignore for now
                    }

                    LocalOnlyHotspotStatus.LocalOnlyHotspotRunning -> {
                        //stop local-only hotspot
                        hotspotReservation?.close()
                        updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.Idle)
                    }

                    LocalOnlyHotspotStatus.Failure -> {
                        createLocalOnlyHotspotNetwork()
                    }
                }
            }

            TcpScreenEvents.DiscoverHotSpotClick -> {
                if (!state.value.isWifiOn) {
                    showWifiNotEnabledDialog()
                    return
                }

                when (state.value.hotspotNetworkingStatus) {
                    HotspotNetworkingStatus.Idle -> {
                        if (hasOtherNetworkingIsRunning()) {
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
                        if (hasOtherNetworkingIsRunning()) {
                            return
                        }
                        emitNavigation(TcpScreenNavigation.OnStartHotspotNetworking)
                    }
                }
            }

            is TcpScreenEvents.SendMessageRequest -> {

                val textMessageEntity = ChatMessageEntity(
                    type = AppMessageType.TEXT,
                    formattedTime = getCurrentTime(),
                    isFromYou = true,
                    userId = state.value.peerUserUniqueId,
                    text = event.message
                )

                //todo - think about this later
                if (state.value.connectionsCount < 1) {
                    updateHasErrorOccurredDialog(TcpScreenDialogErrors.PeerNotConnected)
                    return
                }
                when (state.value.generalConnectionStatus) {
                    GeneralConnectionStatus.Idle -> {
                        //Establish connection to send message
                        log("idle connection")
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
                    }

                    GeneralConnectionStatus.ConnectedAsClient -> {
                        emitNavigation(TcpScreenNavigation.SendClientMessage(textMessageEntity))
                    }

                    GeneralConnectionStatus.ConnectedAsHost -> {
                        emitNavigation(TcpScreenNavigation.SendHostMessage(textMessageEntity))
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
                if (!state.value.isWifiOn) {
                    showWifiNotEnabledDialog()
                    return
                }

                when (state.value.p2pNetworkingStatus) {
                    P2PNetworkingStatus.Idle -> {
                        if (hasOtherNetworkingIsRunning()) {
                            return
                        }
                        emitNavigation(TcpScreenNavigation.OnDiscoverP2PClick)
                    }

                    P2PNetworkingStatus.Discovering -> {
                        emitNavigation(TcpScreenNavigation.OnStopP2PDiscovery)
                    }

                    P2PNetworkingStatus.Failure -> {
                        if (hasOtherNetworkingIsRunning()) {
                            return
                        }
                        emitNavigation(TcpScreenNavigation.OnDiscoverP2PClick)
                    }
                }
            }

            TcpScreenEvents.CreateServerClick -> {

                if (!state.value.isValidPortNumber) {
                    Log.d("ahi3646", "handleEvents: invalid port number ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }
                //todo - CLARIFY WHY WE NEED GROUP ADDRESS
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

                if (!state.value.isWifiOn) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.WifiNotEnabled))
                    return
                }

//                if (state.value.generalNetworkingStatus == GeneralNetworkingStatus.Idle) {
//                    updateHasErrorOccurredDialog(TcpScreenDialogErrors.NO_NETWORK_FOR_CONNECTION)
//                    return
//                }

                if (!state.value.isValidPortNumber) {
                    Log.d("ahi3646", "handleEvents: invalid port number ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }

                if (!state.value.isValidConnectedWifiAddress) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidWiFiServerIpAddress))
                    return
                }

                //validateConfigurationsByNetworkType()

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

            is TcpScreenEvents.OnHotspotPasswordChanged -> {
                viewModelScope.launch {
                    dataStorePreferenceRepository.setHotSpotPassword(event.hotspotPassword)
                }
                _state.update {
                    it.copy(
                        isValidHotSpotPassword = isValidHotspotPassword(event.hotspotPassword),
                        hotspotPassword = event.hotspotPassword.trim()
                    )
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
                log("on general connection status changed 2")
                _state.update {
                    it.copy(
                        clientConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }

            ClientConnectionStatus.Connecting, ClientConnectionStatus.Connected -> {
                _state.update {
                    it.copy(
                        clientConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.ConnectedAsClient
                    )
                }
            }

            ClientConnectionStatus.Failure -> {
                log("on general connection status changed 4")
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
                log("on general connection status changed 5")
                _state.update {
                    it.copy(
                        hostConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }

            HostConnectionStatus.Creating, HostConnectionStatus.Created -> {
                _state.update {
                    it.copy(
                        hostConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.ConnectedAsHost
                    )
                }
            }

            HostConnectionStatus.Failure -> {
                log("on general connection status changed 6")
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

    fun updateLocalOnlyHotspotStatus(status: LocalOnlyHotspotStatus) {
        when (status) {
            LocalOnlyHotspotStatus.Idle -> {
                _state.update {
                    it.copy(
                        localOnlyHotspotNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.Idle
                    )
                }
            }

            LocalOnlyHotspotStatus.LaunchingLocalOnlyHotspot,
            LocalOnlyHotspotStatus.LocalOnlyHotspotRunning -> {
                _state.update {
                    it.copy(
                        localOnlyHotspotNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.LocalOnlyHotspot,
                    )
                }
            }

            LocalOnlyHotspotStatus.Failure -> {
                _state.update {
                    it.copy(
                        localOnlyHotspotNetworkingStatus = status,
                        generalNetworkingStatus = GeneralNetworkingStatus.Idle
                    )
                }
            }
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

            HotspotNetworkingStatus.LaunchingHotspot, HotspotNetworkingStatus.HotspotRunning -> {
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

    fun clearPeersList() {
        _state.update {
            it.copy(
                availableWifiNetworks = emptyList()
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun showWifiNotEnabledDialog() {
        val wifiNotEnabledDialog = VisibleActionDialogs.WifiEnableRequest(
            onPositiveButtonClick = {
                visibleActionDialogQueue.removeFirst()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Use alternative approach
                    emitNavigation(TcpScreenNavigation.WifiEnableRequest)
                } else {
                    // Fallback for older Android versions
                    wifiManager.isWifiEnabled = true
                }
            },
            onNegativeButtonClick = {
                visibleActionDialogQueue.removeFirst()
            }

        )
        visibleActionDialogQueue.add(wifiNotEnabledDialog)
    }

    /**
     * Shows error if other network is running
     * @return true if other network is running, false otherwise
     */
    private fun hasOtherNetworkingIsRunning(): Boolean {
        if (state.value.generalNetworkingStatus != GeneralNetworkingStatus.Idle) {
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.OtherNetworkingIsRunning)
            return true
        } else return false
    }

    fun updateHasErrorOccurredDialog(dialog: TcpScreenDialogErrors?) {
        _state.update {
            it.copy(
                hasDialogErrorOccurred = dialog
            )
        }
    }

    fun updatePercentageOfReceivingFile(message: ChatMessageEntity) {
        when (message.type) {
            AppMessageType.FILE -> {
                viewModelScope.launch(Dispatchers.IO) {
                    messagesDao.updateFileMessage(
                        messageId = message.id,
                        newFileState = message.fileState
                    )
                }
            }

            AppMessageType.VOICE -> {
                log("updating voice message - $message")
                viewModelScope.launch(Dispatchers.IO) {
                    messagesDao.updateVoiceFileMessage(
                        messageId = message.id,
                        newFileState = message.fileState,
                        newDuration = message.voiceMessageAudioFileDuration
                    )
                }
            }

            else -> return
        }
    }

    suspend fun insertMessage(messageEntity: ChatMessageEntity): Long {
        return messagesDao.insertMessage(messageEntity)
    }

    fun updateConnectedDevices(device: WifiP2pDevice) {
        val connectedDevices = state.value.connectedWifiNetworks.toMutableList().apply {
            if (!contains(device)) {
                add(device)
            }
        }
        _state.update {
            it.copy(
                connectedWifiNetworks = connectedDevices
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

sealed interface VisibleActionDialogs {

    val dialogTitle: Int
    val dialogMessage: Int
    val icon: ImageVector
    val positiveButtonText: Int
    val negativeButtonText: Int
    val onPositiveButtonClick: () -> Unit
    val onNegativeButtonClick: () -> Unit

    data class WifiEnableRequest(
        override val dialogTitle: Int = R.string.wifi_not_enabled,
        override val dialogMessage: Int = R.string.wifi_not_enabled_message,
        override val icon: ImageVector = Icons.Default.WifiOff,
        override val positiveButtonText: Int = R.string.enable,
        override val negativeButtonText: Int = R.string.dismiss,
        override val onPositiveButtonClick: () -> Unit = {},
        override val onNegativeButtonClick: () -> Unit = {}
    ) : VisibleActionDialogs
}

data class InitialChatModel(
    val userUniqueId: String,
    val userUniqueName: String,
)