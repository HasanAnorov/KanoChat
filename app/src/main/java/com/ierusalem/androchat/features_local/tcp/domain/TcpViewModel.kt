package com.ierusalem.androchat.features_local.tcp.domain

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.google.gson.Gson
import com.ierusalem.androchat.core.app.AppBroadcastFrequency
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.directory_router.FilesDirectoryService
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.Constants.SOCKET_DEFAULT_BUFFER_SIZE
import com.ierusalem.androchat.core.utils.Constants.getCurrentTime
import com.ierusalem.androchat.core.utils.Constants.getRandomColor
import com.ierusalem.androchat.core.utils.Constants.getTimeInHours
import com.ierusalem.androchat.core.utils.Constants.isValidVersionForLocalOnlyHotspot
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.core.utils.UiText
import com.ierusalem.androchat.core.utils.addLabelBeforeExtension
import com.ierusalem.androchat.core.utils.generateFileFromUri
import com.ierusalem.androchat.core.utils.getAudioFileDuration
import com.ierusalem.androchat.core.utils.getFileByName
import com.ierusalem.androchat.core.utils.isValidHotspotName
import com.ierusalem.androchat.core.utils.isValidHotspotPassword
import com.ierusalem.androchat.core.utils.isValidIpAddress
import com.ierusalem.androchat.core.utils.isValidPortNumber
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.readableFileSize
import com.ierusalem.androchat.core.voice_message.playback.AndroidAudioPlayer
import com.ierusalem.androchat.core.voice_message.recorder.AndroidAudioRecorder
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp.data.server.ServerDefaults
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuard
import com.ierusalem.androchat.features_local.tcp.data.server.wifidirect.Reason
import com.ierusalem.androchat.features_local.tcp.data.server.wifidirect.WiFiNetworkEvent
import com.ierusalem.androchat.features_local.tcp.domain.model.AudioState
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.ClientConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.ContactItem
import com.ierusalem.androchat.features_local.tcp.domain.state.ContactMessageItem
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.HostConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.HotspotNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.InitialUserModel
import com.ierusalem.androchat.features_local.tcp.domain.state.LocalOnlyHotspotStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.P2PNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenDialogErrors
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenErrors
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.domain.state.VisibleActionDialogs
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UTFDataFormatException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class TcpViewModel @Inject constructor(
    private val permissionGuard: PermissionGuard,
    private val dataStorePreferenceRepository: DataStorePreferenceRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val wifiManager: WifiManager,
    private val contentResolver: ContentResolver,
    private val audioRecorder: AndroidAudioRecorder,
    private val audioPlayer: AndroidAudioPlayer,
    private val wifiP2PManager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val messagesRepository: MessagesRepository,
    filesDirectoryService: FilesDirectoryService,
) : ViewModel(), NavigationEventDelegate<TcpScreenNavigation> by DefaultNavigationEventDelegate() {

    //chatting server side
    private lateinit var serverSocket: ServerSocket
    private lateinit var connectedClientSocket: Socket
    private lateinit var connectedClientWriter: DataOutputStream

    //chatting client side
    private lateinit var clientSocket: Socket
    private lateinit var clientWriter: DataOutputStream

    //local only hotspot
    private var hotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null
    private val customGson = Gson()

    private val _state: MutableStateFlow<TcpScreenUiState> = MutableStateFlow(TcpScreenUiState())
    val state: StateFlow<TcpScreenUiState> = _state.asStateFlow()

    val visiblePermissionDialogQueue = mutableStateListOf<String>()
    val visibleActionDialogQueue = mutableStateListOf<VisibleActionDialogs>()

    private val privateFilesDirectory = filesDirectoryService.getPrivateFilesDirectory()

    init {
        initBroadcastFrequency()
        initializeHotspotConfigs()
        listenWifiConnections()
    }

    private val _selectedUser = MutableStateFlow<InitialUserModel?>(null)
    private val playingMessageStream = MutableStateFlow<Pair<Long, AudioState>?>(null)

    fun setSelectedUser(selectedUser: InitialUserModel) {
        _selectedUser.value = selectedUser
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val pagingDataStream = _selectedUser.flatMapLatest { chattingUser ->
        chattingUser?.let {
            Pager(
                PagingConfig(pageSize = 18, prefetchDistance = 25),
                pagingSourceFactory = {
                    messagesRepository.getPagedUserMessagesById(
                        partnerSessionId = it.partnerSessionId,
                        authorSessionId = state.value.authorSessionId
                    )
                }
            ).flow.mapNotNull { value: PagingData<ChatMessageEntity> ->
                value.map { chatMessageEntity ->
                    chatMessageEntity.toChatMessage()
                }
            }.cachedIn(viewModelScope)
        } ?: flowOf(PagingData.empty())
    }

    val messagesStream by lazy {
        playingMessageStream.combine(pagingDataStream, ::Pair)
            .map { (playingMessage, pagingData) ->
                val (messageId, audioState) = playingMessage ?: return@map pagingData
                pagingData.map { msg ->
                    when {
                        // Only update if the target VoiceMessage state needs to change
                        msg is ChatMessage.VoiceMessage && msg.messageId == messageId -> {
                            msg.copy(audioState = audioState)
                        }
                        // Reset other VoiceMessages to Idle if they are not already Idle
                        msg is ChatMessage.VoiceMessage && msg.messageId != messageId -> {
                            msg.copy(audioState = AudioState.Idle)
                        }

                        else -> msg // Return the message unchanged to prevent unnecessary recomposition
                    }
                }
            }
    }

    fun updateFileStateToFailure() {
        viewModelScope.launch(Dispatchers.IO) {
            messagesRepository.updateFileStateToFailure()
        }
    }

    //todo - there is no need to add delay but i can't fix it without delay
    fun logout(onFinished: () -> Unit) {
        viewModelScope.launch {

            when (state.value.generalConnectionStatus) {
                GeneralConnectionStatus.Idle -> {}
                GeneralConnectionStatus.ConnectedAsHost -> {
                    closeClientServerSocket()
                    closeServeSocket()
                    delay(200)
                    updateHostConnectionStatus(HostConnectionStatus.Idle)
                    updateClientConnectionStatus(ClientConnectionStatus.Idle)
                }

                GeneralConnectionStatus.ConnectedAsClient -> {
                    closeClientSocket()
                    delay(200)
                    updateHostConnectionStatus(HostConnectionStatus.Idle)
                    updateClientConnectionStatus(ClientConnectionStatus.Idle)
                }
            }

            when (state.value.generalNetworkingStatus) {
                GeneralNetworkingStatus.Idle -> {}
                GeneralNetworkingStatus.P2PDiscovery -> {
                    stopP2PNetworking()
                }

                GeneralNetworkingStatus.HotspotDiscovery -> {
                    stopHotspotNetworking()
                }

                GeneralNetworkingStatus.LocalOnlyHotspot -> {
                    stopLocalOnLyHotspot()
                }
            }
            //delay for shutting down servers
            delay(200)
            updateHasErrorOccurredDialog(null)
            onFinished()
        }
    }

    /** Initializing Functions*/

    fun loadChattingUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val authorSessionID = runBlocking { dataStorePreferenceRepository.getSessionId.first() }
            log("author session id - $authorSessionID")
            messagesRepository.getAllUsersWithLastMessages(authorSessionID).collect { users ->
                _state.update {
                    it.copy(
                        chattingUsers = Resource.Success(
                            users
                                .sortedBy { user -> !user.isOnline }
                                .map { user -> user.toChattingUser() }
                        )
                    )
                }
            }
        }
    }

    fun initializeAuthorSessionId() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    authorSessionId = dataStorePreferenceRepository.getSessionId.first(),
                    authorUsername = dataStorePreferenceRepository.getUsername.first()
                )
            }
        }
    }

    private fun initializeHotspotConfigs() {
        viewModelScope.launch {
            val savedHotspotName = dataStorePreferenceRepository.getHotspotName.first()
            val savedHotspotPassword = dataStorePreferenceRepository.getHotspotPassword.first()
            val savedPortNumber = dataStorePreferenceRepository.getPortNumber.first()
            _state.update {
                it.copy(
                    isValidHotSpotName = isValidHotspotName(savedHotspotName),
                    hotspotName = savedHotspotName,
                    isValidHotSpotPassword = isValidHotspotPassword(savedHotspotPassword),
                    hotspotPassword = savedHotspotPassword,
                    isValidPortNumber = isValidPortNumber(savedPortNumber),
                    portNumber = savedPortNumber
                )
            }
        }
    }

    private fun initBroadcastFrequency() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedBroadcastFrequency =
                dataStorePreferenceRepository.getBroadcastFrequency.first()
            val broadcastFrequency = try {
                AppBroadcastFrequency.valueOf(savedBroadcastFrequency)
            } catch (e: IllegalArgumentException) {
                AppBroadcastFrequency.FREQUENCY_2_4_GHZ
            }
            _state.update { settingsState ->
                settingsState.copy(
                    networkBand = broadcastFrequency
                )
            }
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    override fun onCleared() {
        super.onCleared()
        when (state.value.generalNetworkingStatus) {
            GeneralNetworkingStatus.Idle -> {}
            GeneralNetworkingStatus.LocalOnlyHotspot -> {
                //stop local-only hotspot
                hotspotReservation?.close()
            }

            GeneralNetworkingStatus.HotspotDiscovery -> {
                stopHotspotNetworking()
            }

            GeneralNetworkingStatus.P2PDiscovery -> {
                stopP2PNetworking()
            }
        }
    }

    private fun initializeUser(writer: DataOutputStream) {
        val userUniqueId = runBlocking { dataStorePreferenceRepository.getSessionId.first() }
        val userUniqueName = runBlocking { dataStorePreferenceRepository.getUsername.first() }
        val initialChatModel = InitialUserModel(
            partnerSessionId = userUniqueId,
            partnerUniqueName = userUniqueName
        )
        val initialChatModelStringForm = customGson.toJson(initialChatModel)
        log("initializing user - $initialChatModelStringForm")

        val type = AppMessageType.INITIAL.identifier.code
        try {
            writer.writeChar(type)
            writer.writeUTF(initialChatModelStringForm)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(
                "ahi3646",
                "sendMessage server: dataOutputStream is closed io exception "
            )
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.IOException)
            try {
                writer.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
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

    fun updateAllUsersOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            messagesRepository.updateAllUsersOnlineStatus(isOnline = isOnline)
        }
    }

    private fun updateUserOnlineStatus(userUniqueId: String, isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            messagesRepository.updateIsUserOnline(
                userUniqueId = userUniqueId,
                isOnline = isOnline
            )
        }
    }

    private fun setupUserData(reader: DataInputStream) {
        val receivedMessage = reader.readUTF()
        log("setup user data - $receivedMessage")

        val initialChattingUserModel = customGson.fromJson(
            receivedMessage,
            InitialUserModel::class.java
        )

        updateInitialChatModel(initialChattingUserModel)

        // Call the function that handles user insertion and online status
        handleUserInsertionAndStatus(initialChattingUserModel)
    }

    // Function to handle both inserting the user and updating their online status
    private fun handleUserInsertionAndStatus(initialChatModel: InitialUserModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val isUserExist =
                messagesRepository.isUserExist(
                    initialChatModel.partnerSessionId,
                    state.value.authorSessionId
                )
            if (isUserExist) {
                log("user exist")
                updateUserOnlineStatus(
                    userUniqueId = initialChatModel.partnerSessionId,
                    isOnline = true
                )
            } else {
                val newChattingUser = ChattingUserEntity(
                    authorSessionId = state.value.authorSessionId,
                    partnerSessionID = initialChatModel.partnerSessionId,
                    partnerUsername = initialChatModel.partnerUniqueName,
                    avatarBackgroundColor = getRandomColor(),
                    isOnline = true,
                    createdAt = getCurrentTime()
                )
                log("new user - $newChattingUser")
                messagesRepository.insertChattingUser(newChattingUser)
            }
        }
    }

    private suspend fun createServer(portNumber: Int) = withContext(Dispatchers.IO) {
        log("creating server ...")
        log("group address - ${state.value.groupOwnerAddress} \ncreating server ...")
        updateHostConnectionStatus(HostConnectionStatus.Creating)

        try {
            serverSocket = ServerSocket(portNumber)
            log("server created in : $serverSocket ${serverSocket.localSocketAddress}")
            if (serverSocket.isBound) {
                updateHostConnectionStatus(HostConnectionStatus.Created)
            }
            while (!serverSocket.isClosed) {
                connectedClientSocket = serverSocket.accept()

                connectedClientSocket.keepAlive = true

                connectedClientWriter =
                    DataOutputStream(connectedClientSocket.getOutputStream())
                //here we sending the unique device id to the client
                initializeUser(writer = connectedClientWriter)
                log("New client : $connectedClientSocket ")

                while (!connectedClientSocket.isClosed) {
                    val reader =
                        DataInputStream(BufferedInputStream(connectedClientSocket.getInputStream()))

                    try {
                        val messageType = AppMessageType.fromChar(reader.readChar())
                        log("create server incoming message type - $messageType ")

                        when (messageType) {
                            AppMessageType.INITIAL -> {
                                setupUserData(reader = reader)
                            }

                            AppMessageType.VOICE -> {
                                receiveVoiceMessage(
                                    reader = reader,
                                    receivingSocket = connectedClientSocket
                                )
                            }

                            AppMessageType.CONTACT -> {
                                viewModelScope.launch(Dispatchers.IO) {
                                    receiveContactMessage(reader = reader)
                                }
                            }

                            AppMessageType.TEXT -> {
                                viewModelScope.launch(Dispatchers.IO) {
                                    receiveTextMessage(reader = reader)
                                }
                            }

                            AppMessageType.FILE -> {
                                receiveFile(
                                    reader = reader,
                                    receivingSocket = connectedClientSocket
                                )
                            }

                            AppMessageType.UNKNOWN -> {
                                /**Ignore case*/
                                /**Ignore case*/
                                log("create server unknown message char - ${reader.readChar()}")
                            }
                        }
                    } catch (e: EOFException) {
                        e.printStackTrace()
                        //if the IP address of the host could not be determined.
                        log("createServer while: EOFException")
                        closeClientServerSocket()
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.EOException)
                        updateUserOnlineStatus(
                            userUniqueId = state.value.peerUserUniqueId,
                            isOnline = false
                        )
                        try {
                            reader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        //the stream has been closed and the contained
                        // input stream does not support reading after close,
                        // or another I/O error occurs
                        log("createServer while: io exception ")
                        closeClientServerSocket()
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.IOException)
                        updateUserOnlineStatus(
                            userUniqueId = state.value.peerUserUniqueId,
                            isOnline = false
                        )
                        try {
                            reader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            log("reader close exception - $e ")
                        }
                    } catch (e: UTFDataFormatException) {
                        e.printStackTrace()
                        //if the bytes do not represent a valid modified UTF-8 encoding of a string.
                        log("createServer while: UTFDataFormatException exception ")
                        closeClientServerSocket()
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.UTFDataFormatException)
                        updateUserOnlineStatus(
                            userUniqueId = state.value.peerUserUniqueId,
                            isOnline = false
                        )
                        try {
                            reader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        log("createServer while: unknown exception ")
                        closeClientServerSocket()
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.UnknownException)
                        updateUserOnlineStatus(
                            userUniqueId = state.value.peerUserUniqueId,
                            isOnline = false
                        )
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            log("createServer: IOException ")
            closeServeSocket()
            updateHostConnectionStatus(HostConnectionStatus.Failure)
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.IOException)
            updateUserOnlineStatus(
                userUniqueId = state.value.peerUserUniqueId,
                isOnline = false
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            //if a security manager exists and its checkConnect method doesn't allow the operation.
            log("createServer: SecurityException ")
            closeServeSocket()
            updateHostConnectionStatus(HostConnectionStatus.Failure)
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.SecurityException)
            updateUserOnlineStatus(
                userUniqueId = state.value.peerUserUniqueId,
                isOnline = false
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            //if the port parameter is outside the specified range of valid port values,
            // which is between 0 and 65535, inclusive.
            log("createServer: IllegalArgumentException ")
            closeServeSocket()
            updateHostConnectionStatus(HostConnectionStatus.Failure)
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.IllegalArgumentException)
            updateUserOnlineStatus(
                userUniqueId = state.value.peerUserUniqueId,
                isOnline = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            log("createServer: unknown exception ")
            closeServeSocket()
            updateHostConnectionStatus(HostConnectionStatus.Failure)
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.UnknownException)
        }
    }

    // network clean up should be carried out in viewmodel
    private fun handleWifiDisabledCase() {
        when (state.value.generalConnectionStatus) {
            GeneralConnectionStatus.Idle -> {
                /** do nothing */
            }

            GeneralConnectionStatus.ConnectedAsClient -> {
                log("connected as client")
                closeClientSocket()
                updateClientConnectionStatus(ClientConnectionStatus.Idle)
                updateUserOnlineStatus(
                    userUniqueId = state.value.peerUserUniqueId,
                    isOnline = false
                )
            }

            GeneralConnectionStatus.ConnectedAsHost -> {
                log("connected as host")
                closeClientServerSocket()
                closeServeSocket()
                updateHostConnectionStatus(HostConnectionStatus.Idle)
                updateUserOnlineStatus(
                    userUniqueId = state.value.peerUserUniqueId,
                    isOnline = false
                )
            }
        }
    }

    private fun closeServeSocket() {
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
    }

    private fun closeClientServerSocket() {
        if (::connectedClientSocket.isInitialized) {
            connectedClientSocket.close()
        }
    }

    private fun closeClientSocket() {
        if (::clientSocket.isInitialized) {
            clientSocket.close()
        }
    }

    private suspend fun connectToServer(serverIpAddress: String, serverPort: Int) =
        withContext(Dispatchers.IO) {
            log("connecting to server - $serverIpAddress:$serverPort")
            try {
                //create client socket
                clientSocket = Socket(serverIpAddress, serverPort)
                clientWriter = DataOutputStream(clientSocket.getOutputStream())
                log("client writer initialized - $clientWriter")
                initializeUser(writer = clientWriter)

                updateClientConnectionStatus(ClientConnectionStatus.Connected)

                //received outcome messages here
                while (!clientSocket.isClosed) {
                    val reader = DataInputStream(BufferedInputStream(clientSocket.getInputStream()))

                    try {
                        val messageType = AppMessageType.fromChar(reader.readChar())
                        log("client server incoming message type - $messageType ")

                        when (messageType) {
                            AppMessageType.INITIAL -> {
                                setupUserData(reader = reader)
                            }

                            AppMessageType.VOICE -> {
                                receiveVoiceMessage(
                                    reader = reader,
                                    receivingSocket = clientSocket
                                )
                            }

                            AppMessageType.CONTACT -> {
                                viewModelScope.launch(Dispatchers.IO) {
                                    receiveContactMessage(reader = reader)
                                }
                            }

                            AppMessageType.TEXT -> {
                                viewModelScope.launch(Dispatchers.IO) {
                                    receiveTextMessage(reader = reader)
                                }
                            }

                            AppMessageType.FILE -> {
                                receiveFile(reader = reader, receivingSocket = clientSocket)
                            }

                            AppMessageType.UNKNOWN -> {
                                /**Ignore case*/
                                log("connect to server unknown message char - ${reader.readChar()}")
                            }
                        }
                    } catch (e: EOFException) {
                        e.printStackTrace()
                        //if the IP address of the host could not be determined.
                        log("connectToServer: EOFException ")
                        closeClientSocket()
                        updateClientConnectionStatus(ClientConnectionStatus.Failure)
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.EOException)
                        updateUserOnlineStatus(
                            userUniqueId = state.value.peerUserUniqueId,
                            isOnline = false
                        )
                        try {
                            reader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        //the stream has been closed and the contained
                        // input stream does not support reading after close,
                        // or another I/O error occurs
                        log("connectToServer: ioexception ")
                        closeClientSocket()
                        updateClientConnectionStatus(ClientConnectionStatus.Failure)
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.IOException)
                        updateUserOnlineStatus(
                            userUniqueId = state.value.peerUserUniqueId,
                            isOnline = false
                        )
                        try {
                            reader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } catch (e: UTFDataFormatException) {
                        e.printStackTrace()
                        //if the bytes do not represent a valid modified UTF-8 encoding of a string.
                        log("connectToServer: UTFDataFormatException exception ")
                        closeClientSocket()
                        updateClientConnectionStatus(ClientConnectionStatus.Failure)
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.UTFDataFormatException)
                        updateUserOnlineStatus(
                            userUniqueId = state.value.peerUserUniqueId,
                            isOnline = false
                        )
                        try {
                            reader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (exception: UnknownHostException) {
                exception.printStackTrace()
                log("unknown host exception".uppercase())
                updateHasErrorOccurredDialog(TcpScreenDialogErrors.UnknownHostException)
                updateClientConnectionStatus(ClientConnectionStatus.Failure)
                updateUserOnlineStatus(
                    userUniqueId = state.value.peerUserUniqueId,
                    isOnline = false
                )
            } catch (exception: IOException) {
                exception.printStackTrace()
                //could not connect to a server
                log("connectToServer: IOException ".uppercase())
                updateHasErrorOccurredDialog(TcpScreenDialogErrors.IOException)
                updateClientConnectionStatus(ClientConnectionStatus.Failure)
                updateUserOnlineStatus(
                    userUniqueId = state.value.peerUserUniqueId,
                    isOnline = false
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
                //if a security manager exists and its checkConnect method doesn't allow the operation.
                log("connectToServer: SecurityException".uppercase())
                updateHasErrorOccurredDialog(TcpScreenDialogErrors.SecurityException)
                updateClientConnectionStatus(ClientConnectionStatus.Failure)
                updateUserOnlineStatus(
                    userUniqueId = state.value.peerUserUniqueId,
                    isOnline = false
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                //if the port parameter is outside the specified range of valid port values,
                // which is between 0 and 65535, inclusive.
                log("connectToServer: IllegalArgumentException".uppercase())
                updateHasErrorOccurredDialog(TcpScreenDialogErrors.IllegalArgumentException)
                updateClientConnectionStatus(ClientConnectionStatus.Failure)
                updateUserOnlineStatus(
                    userUniqueId = state.value.peerUserUniqueId,
                    isOnline = false
                )
            }

        }

    /**Socket Sending Functions*/

    fun sendClientMessage(message: ChatMessageEntity) {
        log("send client message - $message")
        if (!clientSocket.isClosed) {
            when (message.type) {
                AppMessageType.TEXT -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        sendTextMessage(writer = clientWriter, textMessage = message)
                    }
                }

                AppMessageType.VOICE -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        sendVoiceMessage(writer = clientWriter, voiceMessage = message)
                    }
                }

                AppMessageType.CONTACT -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        sendContactMessage(writer = clientWriter, contactMessage = message)
                    }
                }

                AppMessageType.FILE -> {
                    viewModelScope.launch {
                        sendFileMessages(writer = clientWriter, messages = listOf(message))
                    }
                }

                else -> {
                    /** Just ignore */
                    log("unknown message type - ${message.type}")
                }
            }
        } else {
            log("send client message: client socket is closed ")
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
        }
    }

    fun sendHostMessage(message: ChatMessageEntity) {
        log("send host message - $message")
        if (!connectedClientSocket.isClosed) {
            when (message.type) {
                AppMessageType.TEXT -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        sendTextMessage(writer = connectedClientWriter, textMessage = message)
                    }
                }

                AppMessageType.VOICE -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        sendVoiceMessage(writer = connectedClientWriter, voiceMessage = message)
                    }
                }

                AppMessageType.CONTACT -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        sendContactMessage(writer = connectedClientWriter, contactMessage = message)
                    }
                }

                AppMessageType.FILE -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        sendFileMessages(writer = connectedClientWriter, messages = listOf(message))
                    }
                }

                else -> {
                    /** Just ignore */
                    log("unknown message type - ${message.type}")
                }
            }
        } else {
            log("send host message: client socket is closed ")
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
        }
    }

    private fun sendTextMessage(
        writer: DataOutputStream,
        textMessage: ChatMessageEntity
    ) {
        try {
            writer.writeChar(textMessage.type.identifier.code)
            writer.writeUTF(textMessage.text)
            viewModelScope.launch {
                insertMessage(textMessage)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(
                "ahi3646",
                "sendMessage server: dataOutputStream is closed io exception "
            )
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.IOException)
            try {
                writer.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * 1. message type
     * 2. audio file name
     * 3. file length
     * */
    private suspend fun sendVoiceMessage(
        writer: DataOutputStream,
        voiceMessage: ChatMessageEntity
    ) {
        log("sending voice message - $voiceMessage ...")
        withContext(Dispatchers.IO) {

            val messageId = runBlocking(Dispatchers.IO) {
                insertMessage(voiceMessage)
            }
            log("audio message id - $messageId")

            try {

                //sending file type
                val type = AppMessageType.VOICE.identifier.code
                writer.writeChar(type)
                log("sending audio message type - $type")

                //Create File object
                val file = File(privateFilesDirectory, voiceMessage.voiceMessageFileName!!)

                //sending file name
                writer.writeUTF(voiceMessage.voiceMessageFileName)
                log("sending audio message name - ${voiceMessage.voiceMessageFileName}")

                //write length
                writer.writeLong(file.length())
                log("sending audio message file length - ${file.length()}")

                var bytesForPercentage = 0L
                val fileSizeForPercentage = file.length()

                BufferedInputStream(FileInputStream(file)).use { fileInputStream ->
                    // Here we  break file into chunks
                    val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)
                    var bytes: Int

                    while ((fileInputStream.read(buffer).also { bytes = it }) != -1) {
                        // Send the file to Server Socket
                        log("bytes sent - $bytes")
                        writer.write(buffer, 0, bytes)
                        writer.flush()

                        bytesForPercentage += bytes.toLong()
                        val percentage =
                            (bytesForPercentage.toDouble() / fileSizeForPercentage.toDouble() * 100).toInt()
                        val tempPercentage =
                            ((bytesForPercentage - bytes.toLong()) / fileSizeForPercentage.toDouble() * 100).toInt()

                        if (percentage != tempPercentage) {
                            log("audio progress - $percentage")
                            val newVoiceMessage = voiceMessage.copy(
                                id = messageId,
                                fileState = FileMessageState.Loading(percentage)
                            )
                            updatePercentageOfReceivingAudioFile(newVoiceMessage)
                        }
                    }
                }

                // Ensure all bytes were sent
                if (bytesForPercentage == fileSizeForPercentage) {
                    log("All bytes sent correctly.")
                    val newState = FileMessageState.Success
                    val newVoiceMessage = voiceMessage.copy(
                        id = messageId,
                        isFileAvailable = true,
                        fileState = newState,
                    )
                    runBlocking {
                        updatePercentageOfReceivingAudioFile(newVoiceMessage)
                    }
                    log("audio file sent successfully")
                } else {
                    log("Mismatch: Sent $bytesForPercentage out of $fileSizeForPercentage")
                    val newState = FileMessageState.Failure
                    val newVoiceMessage = voiceMessage.copy(fileState = newState, id = messageId)
                    updatePercentageOfReceivingAudioFile(newVoiceMessage)
                    runBlocking {
                        updatePercentageOfReceivingAudioFile(newVoiceMessage)
                    }
                }
            } catch (exception: IOException) {
                exception.printStackTrace()
                log("audio file sent failed - IOException")
                val newState = FileMessageState.Failure
                val newVoiceMessage = voiceMessage.copy(fileState = newState, id = messageId)
                updatePercentageOfReceivingAudioFile(newVoiceMessage)
            } catch (error: Exception) {
                error.printStackTrace()
                log("audio file sent failed - Exception")
                val newState = FileMessageState.Failure
                val newVoiceMessage = voiceMessage.copy(fileState = newState, id = messageId)
                updatePercentageOfReceivingAudioFile(newVoiceMessage)
            }
        }
    }

    private fun sendContactMessage(
        writer: DataOutputStream,
        contactMessage: ChatMessageEntity
    ) {
        val contactsMessageItem = ContactMessageItem(
            contactName = contactMessage.contactName!!,
            contactNumber = contactMessage.contactNumber!!
        )
        val contactsStringForm = customGson.toJson(contactsMessageItem)

        try {
            writer.writeChar(contactMessage.type.identifier.code)
            writer.writeUTF(contactsStringForm)
            viewModelScope.launch {
                insertMessage(contactMessage)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(
                "ahi3646",
                "sendMessage server: dataOutputStream is closed io exception "
            )
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.IOException)
            try {
                writer.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * 1. type
     * 2. file count
     * 3. file name
     * 4. file length
     * */
    private suspend fun sendFileMessages(
        writer: DataOutputStream,
        messages: List<ChatMessageEntity>
    ) = withContext(Dispatchers.IO) {
        log("sending file ...")
        try {
            //sending file type
            val type = AppMessageType.FILE.identifier.code
            writer.writeChar(type)
            log("send file message - $type")

            //sending file count
            writer.writeInt(messages.size)
            log("file count - ${messages.size}")

            messages.forEach { fileMessage ->

                val messageId = runBlocking(Dispatchers.IO) {
                    insertMessage(fileMessage)
                }

                try {
                    //Create File object
                    val file = File(privateFilesDirectory, fileMessage.fileName!!)
                    log("sending file info: file size - ${file.length()} - ${file.name}")

                    //sending file name
                    writer.writeUTF(fileMessage.fileName)
                    log("sending file name - ${fileMessage.fileName}")

                    //write length
                    writer.writeLong(file.length())
                    log("sending file length - ${file.length()}")

                    var bytesForPercentage = 0L
                    val fileSizeForPercentage = file.length()

                    BufferedInputStream(FileInputStream(file)).use { fileInputStream ->
                        // Here we  break file into chunks
                        val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)
                        var bytes: Int

                        while ((fileInputStream.read(buffer).also { bytes = it }) != -1) {
                            // Send the file to Server Socket
                            writer.write(buffer, 0, bytes)
                            writer.flush()

                            bytesForPercentage += bytes.toLong()
                            val percentage =
                                (bytesForPercentage.toDouble() / fileSizeForPercentage.toDouble() * 100).toInt()
                            val tempPercentage =
                                ((bytesForPercentage - bytes.toLong()) / fileSizeForPercentage.toDouble() * 100).toInt()

                            if (percentage != tempPercentage) {
                                log("progress - $percentage")
                                val newFileMessage =
                                    fileMessage.copy(
                                        id = messageId,
                                        fileState = FileMessageState.Loading(percentage)
                                    )
                                updatePercentageOfReceivingFile(newFileMessage)
                            }
                        }
                    }

                    // Ensure all bytes were sent
                    if (bytesForPercentage == fileSizeForPercentage) {
                        log("All bytes sent correctly.")
                        val newFileMessage = fileMessage.copy(
                            id = messageId,
                            isFileAvailable = true,
                            fileState = FileMessageState.Success
                        )
                        updatePercentageOfReceivingFile(newFileMessage)
                        log("file sent successfully")
                    } else {
                        log("Mismatch: Sent $bytesForPercentage out of $fileSizeForPercentage")
                        val newState = FileMessageState.Failure
                        val newFileMessage =
                            fileMessage.copy(fileState = newState, id = messageId)
                        runBlocking {
                            updatePercentageOfReceivingFile(newFileMessage)
                        }
                    }

                } catch (exception: IOException) {
                    exception.printStackTrace()
                    log("file sent failed IOException")
                    val newState = FileMessageState.Failure
                    val newFileMessage = fileMessage.copy(fileState = newState, id = messageId)
                    updatePercentageOfReceivingFile(newFileMessage)
                } catch (error: Exception) {
                    error.printStackTrace()
                    log("file sent failed - Exception")
                    val newState = FileMessageState.Failure
                    val newFileMessage = fileMessage.copy(fileState = newState, id = messageId)
                    updatePercentageOfReceivingFile(newFileMessage)
                } finally {
                    delay(1000)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            log("file sending process failed: ${e.message}")
        }
    }

    /** Socket Receiving Functions*/
    /**
     * 1. type
     * 2. file count
     * 3. file name
     * 4. file length
     * */
    private suspend fun receiveFile(reader: DataInputStream, receivingSocket: Socket) =
        withContext(Dispatchers.IO) {
            log("receiving file ...")

            try {
                // Reading file count
                val fileCount = reader.readInt()
                log("file count - $fileCount")

                for (i in 0 until fileCount) {
                    // Reading file name
                    val filename = reader.readUTF()
                    log("Expected file name - $filename")

                    // Read the expected file size
                    var fileSize: Long = reader.readLong() // read file size
                    log("file size - $fileSize")

                    var bytesForPercentage = 0L
                    val fileSizeForPercentage = fileSize
                    val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)

                    // Create File object
                    val file =
                        getFileByName(
                            fileName = filename,
                            resourceDirectory = privateFilesDirectory
                        )

                    // Create FileOutputStream to write the received file
                    val fileMessageEntity = ChatMessageEntity(
                        type = AppMessageType.FILE,
                        formattedTime = getCurrentTime(),
                        isFromYou = false,
                        partnerSessionId = state.value.peerUserUniqueId,
                        partnerName = state.value.peerUserName,
                        authorSessionId = state.value.authorSessionId,
                        authorUsername = state.value.authorUsername,
                        //message specific fields
                        fileState = FileMessageState.Loading(0),
                        fileName = file.name,
                        fileSize = fileSize.readableFileSize(),
                        fileExtension = file.extension,
                        filePath = file.path,
                    )

                    val messageId = runBlocking(Dispatchers.IO) {
                        insertMessage(fileMessageEntity)
                    }

                    // Using `use` to ensure the fileOutputStream is properly closed
                    FileOutputStream(file).use { fileOutputStream ->
                        try {
                            receivingSocket.setSoTimeout(Constants.FILE_RECEIVE_TIMEOUT)
                            while (fileSize > 0) {
                                val bytesRead = reader.read(
                                    buffer,
                                    0,
                                    min(buffer.size.toDouble(), fileSize.toDouble()).toInt()
                                )

                                // Check if the client has disconnected (read() returns -1 when disconnected)
                                if (bytesRead == -1) {
                                    log("Client disconnected during file transfer")
                                    throw IOException("Client disconnected unexpectedly")
                                }

                                // Write the file
                                fileOutputStream.write(buffer, 0, bytesRead)
                                fileSize -= bytesRead.toLong()

                                // Update progress
                                bytesForPercentage += bytesRead.toLong()
                                val percentage =
                                    (bytesForPercentage.toDouble() / fileSizeForPercentage.toDouble() * 100).toInt()
                                val tempPercentage =
                                    ((bytesForPercentage - bytesRead.toLong()) / fileSizeForPercentage.toDouble() * 100).toInt()

                                if (fileSize > 0 && percentage != tempPercentage) {
                                    log("progress - $percentage")
                                    val newState = FileMessageState.Loading(percentage)
                                    val newFileMessage =
                                        fileMessageEntity.copy(fileState = newState, id = messageId)
                                    updatePercentageOfReceivingFile(newFileMessage)
                                }
                            }

                            // Ensure all bytes were sent
                            if (bytesForPercentage == fileSizeForPercentage) {
                                log("All bytes received correctly.")
                                val newState = FileMessageState.Success
                                val newFileMessage = fileMessageEntity.copy(
                                    fileState = newState,
                                    id = messageId,
                                    isFileAvailable = true
                                )
                                runBlocking {
                                    updatePercentageOfReceivingFile(newFileMessage)
                                }
                                log("file received successfully")
                            } else {
                                log("Mismatch: Sent $bytesForPercentage out of $fileSizeForPercentage")
                                val newState = FileMessageState.Failure
                                val newFileMessage =
                                    fileMessageEntity.copy(fileState = newState, id = messageId)
                                runBlocking {
                                    updatePercentageOfReceivingFile(newFileMessage)
                                }
                            }

                        } catch (e: IOException) {
                            e.printStackTrace()
                            log("file receiving failed: ${e.message}")
                            val newState = FileMessageState.Failure
                            val newFileMessage =
                                fileMessageEntity.copy(fileState = newState, id = messageId)
                            updatePercentageOfReceivingFile(newFileMessage)
                        } catch (e: SocketException) {
                            e.printStackTrace()
                            log("Client disconnected (SocketException): ${e.message}")
                            val newState = FileMessageState.Failure
                            val newFileMessage =
                                fileMessageEntity.copy(fileState = newState, id = messageId)
                            updatePercentageOfReceivingFile(newFileMessage)
                        } finally {
                            //set timeout to 0 which is infinite
                            receivingSocket.soTimeout = Constants.INFINITELY_TIMEOUT
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                log("file receiving process failed: ${e.message}")
                try {
                    reader.close()
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }

    /**
     * 1. message type
     * 2. audio file name
     * 3. file length
     * */
    private fun receiveVoiceMessage(reader: DataInputStream, receivingSocket: Socket) {
        log("receiving voice file ...")

        try {
            //reading audio file name
            val fileName = reader.readUTF()
            log("Expected audio file name - $fileName")

            // Read the expected file size
            var audioFileSize: Long = reader.readLong() // read file size
            log("audio file size - $audioFileSize")

            var bytesForPercentage = 0L
            val fileSizeForPercentage = audioFileSize
            val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)

            //Create File object
            val file =
                getFileByName(fileName = fileName, resourceDirectory = privateFilesDirectory)

            val voiceMessageEntity = ChatMessageEntity(
                type = AppMessageType.VOICE,
                formattedTime = getCurrentTime(),
                isFromYou = false,
                partnerSessionId = state.value.peerUserUniqueId,
                partnerName = state.value.peerUserName,
                authorSessionId = state.value.authorSessionId,
                authorUsername = state.value.authorUsername,
                //message specific fields
                fileState = FileMessageState.Loading(0),
                voiceMessageFileName = file.name,
                voiceMessageAudioFileDuration = 0L,
            )

            val messageId = runBlocking {
                insertMessage(voiceMessageEntity)
            }

            // Using `use` to ensure the fileOutputStream is properly closed
            FileOutputStream(file).use { fileOutputStream ->
                try {
                    receivingSocket.setSoTimeout(Constants.FILE_RECEIVE_TIMEOUT)
                    while (audioFileSize > 0) {
                        val bytesRead = reader.read(
                            buffer,
                            0,
                            min(buffer.size.toDouble(), audioFileSize.toDouble()).toInt()
                        )
                        log("Read bytes: $bytesRead, Remaining file size: $audioFileSize")

                        // Check if the client has disconnected (read() returns -1 when disconnected)
                        if (bytesRead == -1) {
                            log("Client disconnected during file transfer")
                            throw IOException("Client disconnected unexpectedly")
                        }

                        // Write the file
                        fileOutputStream.write(buffer, 0, bytesRead)
                        audioFileSize -= bytesRead.toLong()

                        // Update progress
                        bytesForPercentage += bytesRead.toLong()
                        val percentage =
                            (bytesForPercentage.toDouble() / fileSizeForPercentage.toDouble() * 100).toInt()
                        val tempPercentage =
                            ((bytesForPercentage - bytesRead.toLong()) / fileSizeForPercentage.toDouble() * 100).toInt()

                        if (audioFileSize > 0 && percentage != tempPercentage) {
                            log("progress - $percentage")
                            val newState = FileMessageState.Loading(percentage)
                            val newFileMessage =
                                voiceMessageEntity.copy(fileState = newState, id = messageId)
                            updatePercentageOfReceivingAudioFile(newFileMessage)
                        }
                    }

                    // Ensure all bytes were sent
                    if (bytesForPercentage == fileSizeForPercentage) {
                        log("All bytes received correctly.")
                        val newState = FileMessageState.Success
                        val newVoiceMessage = voiceMessageEntity.copy(
                            id = messageId,
                            isFileAvailable = true,
                            fileState = newState,
                            voiceMessageAudioFileDuration = file.getAudioFileDuration()
                        )
                        runBlocking {
                            updatePercentageOfReceivingAudioFile(newVoiceMessage)
                        }
                        log("audio file received successfully")
                    } else {
                        log("Mismatch: Sent $bytesForPercentage out of $fileSizeForPercentage")
                        val newState = FileMessageState.Failure
                        val newFileMessage =
                            voiceMessageEntity.copy(fileState = newState, id = messageId)
                        runBlocking {
                            updatePercentageOfReceivingAudioFile(newFileMessage)
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    log("audio file receiving failed: ${e.message}")
                    val newState = FileMessageState.Failure
                    val newFileMessage =
                        voiceMessageEntity.copy(fileState = newState, id = messageId)
                    updatePercentageOfReceivingAudioFile(newFileMessage)
                } catch (e: SocketException) {
                    e.printStackTrace()
                    log("Client disconnected (SocketException): ${e.message}")
                    val failureState =
                        voiceMessageEntity.copy(
                            fileState = FileMessageState.Failure,
                            id = messageId
                        )
                    updatePercentageOfReceivingAudioFile(failureState)
                } finally {
                    //set timeout to 0 which is infinite
                    receivingSocket.soTimeout = Constants.INFINITELY_TIMEOUT
                    log("audio file receiving process finished")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            log("audio file receiving process failed: ${e.message}")
        }
    }

    private fun receiveTextMessage(reader: DataInputStream) {
        val receivedMessage = reader.readUTF()
        log("host incoming text message - $receivedMessage")

        val textMessageEntity = ChatMessageEntity(
            type = AppMessageType.TEXT,
            formattedTime = getCurrentTime(),
            isFromYou = false,
            partnerSessionId = state.value.peerUserUniqueId,
            partnerName = state.value.peerUserName,
            authorSessionId = state.value.authorSessionId,
            authorUsername = state.value.authorUsername,
            //message specific fields
            text = receivedMessage.toString()
        )
        viewModelScope.launch(Dispatchers.IO) {
            insertMessage(textMessageEntity)
        }
    }

    private fun receiveContactMessage(reader: DataInputStream) {
        val receivedMessage = reader.readUTF()
        log("host incoming contact message - $receivedMessage")

        val contactMessageItem =
            Gson().fromJson(
                receivedMessage,
                ContactMessageItem::class.java
            )

        val contactMessageEntity = ChatMessageEntity(
            type = AppMessageType.CONTACT,
            formattedTime = getCurrentTime(),
            isFromYou = false,
            partnerSessionId = state.value.peerUserUniqueId,
            partnerName = state.value.peerUserName,
            authorSessionId = state.value.authorSessionId,
            authorUsername = state.value.authorUsername,
            //message specific fields
            contactName = contactMessageItem.contactName,
            contactNumber = contactMessageItem.contactNumber
        )
        viewModelScope.launch(Dispatchers.IO) {
            insertMessage(contactMessageEntity)
        }
    }

    /** Hotspot networking creation*/

    private fun startHotspotNetworking() {
        viewModelScope.launch(Dispatchers.IO) {
            if (permissionGuard.canCreateNetwork()) {
                createGroup()
            } else {
                log("Permissions not granted for location!")
                // request at leas one time location permission,
                // this make requestPermissionForRationale return true
                emitNavigation(TcpScreenNavigation.RequestLocationPermission)
            }
        }
    }

    private fun stopHotspotNetworking() {
        //close socket only when serverSocket is initialized
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
        if (::connectedClientSocket.isInitialized) {
            connectedClientSocket.close()
        }

        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("Wifi P2P Channel is removed")
                updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Idle)
                handleNetworkEvents(
                    WiFiNetworkEvent.ConnectionStatusChanged(
                        GeneralConnectionStatus.Idle
                    )
                )
                handleNetworkEvents(WiFiNetworkEvent.UpdateGroupOwnerAddress("Not connected"))
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Failed to stop network: ${r.displayReason}")
            }
        }
        wifiP2PManager.removeGroup(channel, listener)
    }

    private fun updateStaticHotspotNameAndPassword(name: String, password: String) {
        _state.update {
            it.copy(
                staticHotspotName = name,
                staticHotspotPassword = password
            )
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun createGroup() {
        updateHotspotDiscoveryStatus(HotspotNetworkingStatus.LaunchingHotspot)
        val config = getConfiguration()
        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("New network created")
                updateHotspotDiscoveryStatus(HotspotNetworkingStatus.HotspotRunning)


                // Delay before requesting group info to ensure group is fully established
                Handler(Looper.getMainLooper()).postDelayed({
                    wifiP2PManager.requestGroupInfo(channel) { group ->
                        if (group != null) {
                            val ssid = group.networkName    // The SSID of the hotspot
                            val passphrase = group.passphrase // The password of the hotspot
                            log("Group SSID: $ssid")
                            log("Group Passphrase: $passphrase")
                            if (!ServerDefaults.canUseCustomConfig()) {
                                updateStaticHotspotNameAndPassword(
                                    name = ssid,
                                    password = passphrase
                                )
                            }
                        } else {
                            log("Group info not available")
                        }
                    }
                }, 1000) // Add a 2-second delay
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Unable to create Wifi Direct Group - ${r.displayReason}")
                updateHasErrorOccurredDialog(TcpScreenDialogErrors.FailedToCreateHotspot)
                updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Failure)
            }
        }

        if (config != null) {
            log("Creating group with configuration")
            wifiP2PManager.createGroup(channel, config, listener)
        } else {
            log("Creating group without custom configuration")
            wifiP2PManager.createGroup(channel, listener)
        }
    }

    private fun getConfiguration(): WifiP2pConfig? {
        if (!ServerDefaults.canUseCustomConfig()) {
            return null
        }

        val ssid = ServerDefaults.asSsid(
            //here you have to return preferred ssid from data store or preference helper
            state.value.hotspotName
        )

        val password = state.value.hotspotPassword

        //here you have to return preferred wifi band like 2,4hz or 5hz
        val networkBand = state.value.networkBand
        val band = when (networkBand) {
            AppBroadcastFrequency.FREQUENCY_2_4_GHZ -> {
                WifiP2pConfig.GROUP_OWNER_BAND_2GHZ
            }

            AppBroadcastFrequency.FREQUENCY_5_GHZ -> {
                WifiP2pConfig.GROUP_OWNER_BAND_5GHZ
            }
        }
        return WifiP2pConfig
            .Builder()
            .setNetworkName(ssid)
            .setPassphrase(password)
            .setGroupOperatingBand(band)
            .build()
    }

    /****/

    /** Wifi P2P networking creation */
    @SuppressLint("MissingPermission")
    private fun startP2PNetworking() {
        viewModelScope.launch(Dispatchers.IO) {
            if (permissionGuard.canCreateNetwork()) {

                val listener = object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        log("onSuccess: discover ")
                        updateP2PDiscoveryStatus(P2PNetworkingStatus.Discovering)
                    }

                    override fun onFailure(reason: Int) {
                        // Code for when the discovery initiation fails goes here.
                        // Alert the user that something went wrong.
                        log("onFailure: discover $reason ")
                        updateP2PDiscoveryStatus(P2PNetworkingStatus.Failure)
                    }
                }
                wifiP2PManager.discoverPeers(channel, listener)
            } else {
                log("Permissions not granted!")
                emitNavigation(TcpScreenNavigation.RequestLocationPermission)
            }
        }
    }

    private fun stopP2PNetworking() {
        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("Wifi P2P Discovery is stopped")
                clearPeersList()
                updateP2PDiscoveryStatus(P2PNetworkingStatus.Idle)
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Failed to stop p2p discovery: ${r.displayReason}")
            }
        }
        wifiP2PManager.stopPeerDiscovery(channel, listener)
    }

    @SuppressLint("MissingPermission")
    private fun connectToWifi(wifiP2pDevice: WifiP2pDevice) {
        log("connectToWifi: $wifiP2pDevice ")
        val config = WifiP2pConfig().apply {
            deviceAddress = wifiP2pDevice.deviceAddress
            wps.setup = WpsInfo.PBC
        }
        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now
                log("success: connected to wifi - ${wifiP2pDevice.deviceAddress}")
                updateConnectedDevices(wifiP2pDevice)
            }

            override fun onFailure(reason: Int) {
                log("failure: failure on wifi connection ")
                emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.FailedToConnectToWifiDevice))
            }
        }
        wifiP2PManager.connect(channel, config, listener)
    }

    /******/

    fun dismissPermissionDialog() {
        log("dismissPermissionDialog")
        visiblePermissionDialogQueue.forEach {
            log("visible permission - $it")
        }
        visiblePermissionDialogQueue.clear()
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
                        isReadContactsGranted = isGranted,
                    )
                }
            }

            Manifest.permission.RECORD_AUDIO -> {
                _state.update {
                    it.copy(
                        isRecordAudioGranted = it.isRecordAudioGranted.apply { value = isGranted },
                    )
                }
            }
        }
    }

    private fun updateInitialChatModel(initialChatModel: InitialUserModel) {
        _state.update {
            it.copy(
                peerUserUniqueId = initialChatModel.partnerSessionId,
                peerUserName = initialChatModel.partnerUniqueName
            )
        }
    }

    private fun startCollectingPlayTiming(messageId: Long) {
        viewModelScope.launch {
            audioPlayer
                .playTiming
                .distinctUntilChanged()
                .collect { timing ->
                    playingMessageStream.emit(messageId to AudioState.Playing(timing))
                }
        }
    }

    fun checkRecordAudioAndReadContactsPermission() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isReadContactsGranted = permissionGuard.canAccessContacts(),
                    isRecordAudioGranted = it.isRecordAudioGranted.apply {
                        value = permissionGuard.canRecordAudio()
                    }
                )
            }
        }
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
            GeneralNetworkingStatus.Idle -> {
                handleWifiDisabledCase()
            }

            GeneralNetworkingStatus.LocalOnlyHotspot -> {
                //ignore case
            }

            GeneralNetworkingStatus.P2PDiscovery -> {
                handleWifiDisabledCase()
                updateP2PDiscoveryStatus(P2PNetworkingStatus.Idle)
            }

            GeneralNetworkingStatus.HotspotDiscovery -> {
                handleWifiDisabledCase()
                updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Idle)
            }
        }
    }

    /**
     * Voice message recording functions
     * */

    private lateinit var currentRecordingAudioFile: File

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

        currentRecordingAudioFile = File(privateFilesDirectory, currentAudioFileName).also {
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
            partnerSessionId = state.value.peerUserUniqueId,
            partnerName = state.value.peerUserName,
            authorSessionId = state.value.authorSessionId,
            authorUsername = state.value.authorUsername,
            //message specific fields
            fileState = FileMessageState.Loading(0),
            voiceMessageFileName = currentRecordingAudioFile.name,
            voiceMessageAudioFileDuration = currentRecordingAudioFile.getAudioFileDuration(),
        )

        when (state.value.generalConnectionStatus) {
            GeneralConnectionStatus.Idle -> {
                log("recording voice message on idle connection")
                cancelRecording()
            }

            GeneralConnectionStatus.ConnectedAsClient -> {
                sendClientMessage(voiceMessageEntity)
            }

            GeneralConnectionStatus.ConnectedAsHost -> {
                sendHostMessage(voiceMessageEntity)
            }
        }
    }

    private fun cancelRecording() {
        updateIsRecording(false)
        audioRecorder.stopAudio()
        if (currentRecordingAudioFile.exists()) {
            val isDeleted = currentRecordingAudioFile.delete()
            log("is cancelled audio deleted - $isDeleted")
        } else {
            log("file does not exist but cancel record called")
        }
    }

    /*****/

    /**
     * Voice message playing functions
     * */

    private var currentPlayingAudioFile: File? = null

    private fun playAudioFile(voiceMessage: ChatMessage.VoiceMessage) {
        currentPlayingAudioFile = File(privateFilesDirectory, voiceMessage.voiceFileName)
        audioPlayer.playAudioFile(currentPlayingAudioFile!!) {
            viewModelScope.launch {
                playingMessageStream.emit(voiceMessage.messageId to AudioState.Idle)
            }
        }
        startCollectingPlayTiming(voiceMessage.messageId)
    }

    private fun resumeAudioFile(voiceMessage: ChatMessage.VoiceMessage) {
        val voiceMessageId = voiceMessage.messageId
        val position = (voiceMessage.audioState as AudioState.Paused).currentPosition
        currentPlayingAudioFile?.let { playingAudioFile ->
            audioPlayer.resumeAudioFile(playingAudioFile, position) {
                viewModelScope.launch {
                    playingMessageStream.emit(voiceMessageId to AudioState.Idle)
                }
            }
            startCollectingPlayTiming(voiceMessageId)
        }
    }

    private fun pauseAudioFile(voiceMessageId: Long) {
        audioPlayer.pause()?.let { position ->
            viewModelScope.launch {
                playingMessageStream.emit(voiceMessageId to AudioState.Paused(position))
            }
        }
    }

    private fun stopAudioFile(voiceMessageId: Long) {
        audioPlayer.stop()
        viewModelScope.launch {
            playingMessageStream.emit(voiceMessageId to AudioState.Idle)
        }
    }

    /******/

    private fun createLocalOnlyHotspotNetwork() {
        viewModelScope.launch(Dispatchers.IO) {
            if (permissionGuard.canCreateLocalOnlyHotSpotNetwork()) {
                startLocalOnlyHotspot()
            } else {
                log("Permissions not granted for location!")
                emitNavigation(TcpScreenNavigation.RequestLocationPermission)
            }
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    @Suppress("DEPRECATION")
    private fun startLocalOnlyHotspot() {
        updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.LaunchingLocalOnlyHotspot)
        val callback = object : LocalOnlyHotspotCallback() {
            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                super.onStarted(reservation)
                log("Local Only Hotspot Started")

                hotspotReservation = reservation

                val config: WifiConfiguration? = reservation?.wifiConfiguration
                log("SSID: ${config?.SSID}, Password: ${config?.preSharedKey}")
                _state.update {
                    it.copy(
                        localOnlyHotspotName = config?.SSID ?: "",
                        localOnlyHotspotPassword = config?.preSharedKey ?: ""
                    )
                }

                val ip = connectivityObserver.getWifiServerIpAddress()
                log("wifi ip local-only is : $ip")

                handleNetworkEvents(WiFiNetworkEvent.UpdateGroupOwnerAddress(ip))
                updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.LocalOnlyHotspotRunning)
            }

            override fun onStopped() {
                super.onStopped()
                log("Local Only Hotspot Stopped")
                updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.Idle)
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                log("Local Only Hotspot Failed")
                updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.Failure)
            }
        }
        wifiManager.startLocalOnlyHotspot(callback, null)
    }

    private fun updateLocalOnlyHotspotStatus(status: LocalOnlyHotspotStatus) {
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

    @SuppressLint("NewApi")
    private fun stopLocalOnLyHotspot() {
        //stop local-only hotspot
        hotspotReservation?.close()
        updateLocalOnlyHotspotStatus(LocalOnlyHotspotStatus.Idle)
    }

    suspend fun getCurrentChattingUser(currentChattingUser: InitialUserModel) {
        messagesRepository.getChattingUserByIdFlow(currentChattingUser.partnerSessionId)
            .collect { user ->
                user?.let {
                    _state.update {
                        it.copy(
                            currentChattingUser = Resource.Success(user.toChattingUser())
                        )
                    }
                }
            }
    }

    fun updateBottomSheetVisibility(shouldBeShown: Boolean) {
        _state.update {
            it.copy(
                showBottomSheet = shouldBeShown
            )
        }
    }

    @SuppressLint("NewApi")
    fun handleEvents(event: TcpScreenEvents) {
        when (event) {

            TcpScreenEvents.OnVoiceRecordStart -> {
                startRecording()
            }

            TcpScreenEvents.OnVoiceRecordFinished -> {
                finishRecording()
            }

            TcpScreenEvents.OnVoiceRecordCancelled -> {
                cancelRecording()
            }

            is TcpScreenEvents.OnPlayVoiceMessageClick -> {
                playAudioFile(event.message)
            }

            is TcpScreenEvents.OnResumeVoiceMessageClick -> {
                resumeAudioFile(event.message)
            }

            is TcpScreenEvents.OnPauseVoiceMessageClick -> {
                pauseAudioFile(event.messageId)
            }

            is TcpScreenEvents.OnStopVoiceMessageClick -> {
                stopAudioFile(event.messageId)
            }

            is TcpScreenEvents.OnSaveToDownloadsClick -> {
                saveFileToDownloads(fileName = event.message.fileName)
            }

            is TcpScreenEvents.TcpChatItemClicked -> {
                emitNavigation(TcpScreenNavigation.OnChattingUserClicked(Gson().toJson(event.currentChattingUser.toInitialChatModel())))
            }

            TcpScreenEvents.RequestRecordAudioPermission -> {
                emitNavigation(TcpScreenNavigation.RequestRecordAudioPermission)
            }

            is TcpScreenEvents.OnConnectToWifiClick -> {
                connectToWifi(event.wifiDevice)
            }

            is TcpScreenEvents.OnContactItemClick -> {
                emitNavigation(TcpScreenNavigation.OnContactItemClick(event.message))
            }

            TcpScreenEvents.ShowFileChooserClick -> {
                emitNavigation(TcpScreenNavigation.ShowFileChooserClick)
            }

            is TcpScreenEvents.OnFileItemClick -> {
                emitNavigation(
                    TcpScreenNavigation.OnFileItemClick(
                        message = event.message,
                        fileDirectory = privateFilesDirectory
                    )
                )
            }

            is TcpScreenEvents.UpdateBottomSheetState -> {
                updateBottomSheetVisibility(event.shouldBeShown)
            }

            TcpScreenEvents.ReadContacts -> {
                readContacts()
            }

            is TcpScreenEvents.HandlePickingMultipleMedia -> {
                viewModelScope.launch(Dispatchers.IO) {
                    handlePickingMultipleMedia(event.medias)
                }
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

            TcpScreenEvents.DiscoverLocalOnlyHotSpotClick -> {
                when (state.value.localOnlyHotspotNetworkingStatus) {
                    LocalOnlyHotspotStatus.Idle -> {
                        if (hasOtherNetworkingIsRunning()) {
                            return
                        }
                        if (!isValidVersionForLocalOnlyHotspot()) {
                            updateHasErrorOccurredDialog(TcpScreenDialogErrors.LocalOnlyHotspotNotSupported)
                            return
                        }
                        createLocalOnlyHotspotNetwork()
                    }

                    LocalOnlyHotspotStatus.LaunchingLocalOnlyHotspot -> {
                        //ignore for now
                    }

                    LocalOnlyHotspotStatus.LocalOnlyHotspotRunning -> {
                        stopLocalOnLyHotspot()
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
                        startHotspotNetworking()
                    }

                    HotspotNetworkingStatus.LaunchingHotspot -> {
                        //just ignore action or implement relaunching feature
                    }

                    HotspotNetworkingStatus.HotspotRunning -> {
                        stopHotspotNetworking()
                        //emitNavigation(TcpScreenNavigation.OnStopHotspotNetworking)
                    }

                    HotspotNetworkingStatus.Failure -> {
                        if (hasOtherNetworkingIsRunning()) {
                            return
                        }
                        startHotspotNetworking()
                        //emitNavigation(TcpScreenNavigation.OnStartHotspotNetworking)
                    }
                }
            }

            is TcpScreenEvents.SendMessageRequest -> {

                val textMessageEntity = ChatMessageEntity(
                    type = AppMessageType.TEXT,
                    formattedTime = getCurrentTime(),
                    isFromYou = true,
                    partnerSessionId = state.value.peerUserUniqueId,
                    partnerName = state.value.peerUserName,
                    authorSessionId = state.value.authorSessionId,
                    authorUsername = state.value.authorUsername,
                    //message specific fields
                    text = event.message
                )

                when (state.value.generalConnectionStatus) {
                    GeneralConnectionStatus.Idle -> {
                        //Establish connection to send message
                        log("idle connection")
                        updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
                    }

                    GeneralConnectionStatus.ConnectedAsClient -> {
                        sendClientMessage(textMessageEntity)
                    }

                    GeneralConnectionStatus.ConnectedAsHost -> {
                        sendHostMessage(textMessageEntity)
                    }
                }
            }

            is TcpScreenEvents.OnPortNumberChanged -> {
                viewModelScope.launch {
                    dataStorePreferenceRepository.setPortNumber(event.portNumber)
                }
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
                        startP2PNetworking()
                    }

                    P2PNetworkingStatus.Discovering -> {
                        stopP2PNetworking()
                    }

                    P2PNetworkingStatus.Failure -> {
                        if (hasOtherNetworkingIsRunning()) {
                            return
                        }
                        startP2PNetworking()
                    }
                }
            }

            TcpScreenEvents.CreateServerClick -> {

                if (!state.value.isValidPortNumber) {
                    Log.d("ahi3646", "handleEvents: invalid port number ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }

                if (state.value.generalNetworkingStatus == GeneralNetworkingStatus.Idle) {
                    updateHasErrorOccurredDialog(TcpScreenDialogErrors.ServerCreationWithoutNetworking)
                    return
                }

                //this when loop determines the state of wi fi connection
                when (state.value.hostConnectionStatus) {
                    HostConnectionStatus.Idle -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            createServer(portNumber = state.value.portNumber.toInt())
                        }
                    }

                    HostConnectionStatus.Creating -> {
                        //creating server ignore for now
                    }

                    HostConnectionStatus.Created -> {
                        //request for stop

                    }

                    HostConnectionStatus.Failure -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            createServer(portNumber = state.value.portNumber.toInt())
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
                    Log.d("ahi3646", "handleEvents: invalid port number ")
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidPortNumber))
                    return
                }

                if (!state.value.isValidConnectedWifiAddress) {
                    emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.InvalidWiFiServerIpAddress))
                    return
                }

                when (state.value.clientConnectionStatus) {
                    ClientConnectionStatus.Idle -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            connectToServer(
                                serverIpAddress = state.value.connectedWifiAddress,
                                serverPort = state.value.portNumber.toInt()
                            )
                        }
                    }

                    ClientConnectionStatus.Connecting -> {
                        log("client is connecting")
                    }

                    ClientConnectionStatus.Connected -> {
                        log("client is connected")
                    }

                    ClientConnectionStatus.Failure -> {
                        log("client is failure")
                        viewModelScope.launch(Dispatchers.IO) {
                            connectToServer(
                                serverIpAddress = state.value.connectedWifiAddress,
                                serverPort = state.value.portNumber.toInt()
                            )
                        }
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

    private fun saveFileToDownloads(fileName: String) {
        val file = File(privateFilesDirectory, fileName)
        val fileNameWithLabel = fileName.addLabelBeforeExtension()

        // Create a custom folder in Downloads directory
        val downloadsDirectoryWithAppFolder =
            Environment.DIRECTORY_DOWNLOADS + "/${Constants.FILE_NAME_LABEL}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped Storage - Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileNameWithLabel)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, downloadsDirectoryWithAppFolder)
            }

            val uri =
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                val inputStream: InputStream = file.inputStream()

                outputStream?.use { out ->
                    inputStream.copyTo(out)
                }

                inputStream.close()
                outputStream?.close()
            }
        } else {
            // Legacy method - Android 9 and below
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destinationFile = File(downloadsDir, fileNameWithLabel)

            file.copyTo(destinationFile, overwrite = true)
        }
    }

    fun handleFilesLauncher(fileUri: Uri) {
        val file = generateFileFromUri(
            contentResolver = contentResolver,
            resourceDirectory = privateFilesDirectory,
            uri = fileUri
        )

        val fileMessageEntity = ChatMessageEntity(
            type = AppMessageType.FILE,
            formattedTime = getCurrentTime(),
            isFromYou = true,
            partnerSessionId = state.value.peerUserUniqueId,
            partnerName = state.value.peerUserName,
            authorSessionId = state.value.authorSessionId,
            authorUsername = state.value.authorUsername,
            //message specific fields
            filePath = file.path,
            fileState = FileMessageState.Loading(0),
            fileName = file.name,
            fileSize = file.length().readableFileSize(),
            fileExtension = file.extension,
        )

        when (state.value.generalConnectionStatus) {
            GeneralConnectionStatus.Idle -> {
                /** do nothing here */
            }

            GeneralConnectionStatus.ConnectedAsClient -> {
                sendClientMessage(fileMessageEntity)
            }

            GeneralConnectionStatus.ConnectedAsHost -> {
                sendHostMessage(fileMessageEntity)
            }
        }
    }

    private fun handlePickingMultipleMedia(medias: List<Uri>) {
        when (state.value.generalConnectionStatus) {
            GeneralConnectionStatus.Idle -> {
                updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
            }

            GeneralConnectionStatus.ConnectedAsHost -> {
                val fileMessages = mutableListOf<ChatMessageEntity>()
                medias.forEach { imageUri ->
                    val file = generateFileFromUri(
                        contentResolver = contentResolver,
                        uri = imageUri,
                        resourceDirectory = privateFilesDirectory
                    )

                    val fileMessageEntity = ChatMessageEntity(
                        type = AppMessageType.FILE,
                        formattedTime = getCurrentTime(),
                        isFromYou = true,
                        partnerSessionId = state.value.peerUserUniqueId,
                        partnerName = state.value.peerUserName,
                        authorSessionId = state.value.authorSessionId,
                        authorUsername = state.value.authorUsername,
                        //message specific fields
                        fileState = FileMessageState.Loading(0),
                        fileName = file.name,
                        fileSize = file.length().readableFileSize(),
                        fileExtension = file.extension,
                        filePath = file.path,
                    )
                    fileMessages.add(fileMessageEntity)
                }
                viewModelScope.launch(Dispatchers.IO) {
                    sendFileMessages(
                        writer = connectedClientWriter,
                        messages = fileMessages
                    )
                }
            }

            GeneralConnectionStatus.ConnectedAsClient -> {
                val fileMessages = mutableListOf<ChatMessageEntity>()
                medias.forEach { imageUri ->
                    val file = generateFileFromUri(contentResolver, imageUri, privateFilesDirectory)
                    val fileMessageEntity = ChatMessageEntity(
                        type = AppMessageType.FILE,
                        formattedTime = getCurrentTime(),
                        isFromYou = true,
                        partnerSessionId = state.value.peerUserUniqueId,
                        partnerName = state.value.peerUserName,
                        authorSessionId = state.value.authorSessionId,
                        authorUsername = state.value.authorUsername,
                        //message specific fields
                        fileState = FileMessageState.Loading(0),
                        fileName = file.name,
                        fileSize = file.length().readableFileSize(),
                        fileExtension = file.extension,
                        filePath = file.path,
                    )
                    fileMessages.add(fileMessageEntity)
                }
                viewModelScope.launch(Dispatchers.IO) {
                    sendFileMessages(
                        writer = clientWriter,
                        messages = fileMessages
                    )
                }
            }
        }
    }

    private fun updateClientConnectionStatus(status: ClientConnectionStatus) {

        when (status) {
            ClientConnectionStatus.Idle -> {
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
                _state.update {
                    it.copy(
                        clientConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }
        }
    }

    private fun updateHostConnectionStatus(status: HostConnectionStatus) {
        when (status) {
            HostConnectionStatus.Idle -> {
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

                _state.update {
                    it.copy(
                        hostConnectionStatus = status,
                        generalConnectionStatus = GeneralConnectionStatus.Idle
                    )
                }
            }
        }
    }

    private fun updateP2PDiscoveryStatus(status: P2PNetworkingStatus) {
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

    private fun updateHotspotDiscoveryStatus(status: HotspotNetworkingStatus) {
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

    private fun clearPeersList() {
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

    private fun updateHasErrorOccurredDialog(dialog: TcpScreenDialogErrors?) {
        _state.update {
            it.copy(
                hasDialogErrorOccurred = dialog
            )
        }
    }

    private suspend fun updatePercentageOfReceivingFile(fileMessage: ChatMessageEntity) {
        messagesRepository.updateFileMessage(
            messageId = fileMessage.id,
            newFileState = fileMessage.fileState,
            isFileAvailable = fileMessage.isFileAvailable
        )
    }

    private fun updatePercentageOfReceivingAudioFile(voiceMessage: ChatMessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            messagesRepository.updateVoiceFileMessage(
                messageId = voiceMessage.id,
                newFileState = voiceMessage.fileState,
                isFileAvailable = voiceMessage.isFileAvailable,
                newDuration = voiceMessage.voiceMessageAudioFileDuration
            )
        }
    }

    private suspend fun insertMessage(messageEntity: ChatMessageEntity): Long {
        return messagesRepository.insertMessage(messageEntity)
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

    fun handleAvailableWifiListChange(peers: List<WifiP2pDevice>) {
        _state.update {
            it.copy(
                availableWifiNetworks = peers
            )
        }
    }

}