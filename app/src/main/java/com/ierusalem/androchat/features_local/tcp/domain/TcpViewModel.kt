package com.ierusalem.androchat.features_local.tcp.domain

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
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
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppBroadcastFrequency
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.ui.navigation.DefaultNavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.NavigationEventDelegate
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.Constants.SOCKET_DEFAULT_BUFFER_SIZE
import com.ierusalem.androchat.core.utils.Constants.getCurrentTime
import com.ierusalem.androchat.core.utils.Constants.getTimeInHours
import com.ierusalem.androchat.core.utils.Json.gson
import com.ierusalem.androchat.core.utils.RandomColors
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.core.utils.UiText
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
import com.ierusalem.androchat.features_local.tcp.domain.model.ChattingUser
import com.ierusalem.androchat.features_local.tcp.domain.state.ClientConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.ContactItem
import com.ierusalem.androchat.features_local.tcp.domain.state.ContactMessageItem
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.HostConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.HotspotNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.LocalOnlyHotspotStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.P2PNetworkingStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenDialogErrors
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenErrors
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenUiState
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.TcpScreenNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
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
import java.io.UTFDataFormatException
import java.net.ServerSocket
import java.net.Socket
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
    private val messagesRepository: MessagesRepository
) : ViewModel(), NavigationEventDelegate<TcpScreenNavigation> by DefaultNavigationEventDelegate() {

    //chatting server side
    private lateinit var serverSocket: ServerSocket
    private lateinit var connectedClientSocket: Socket
    private lateinit var connectedClientWriter: DataOutputStream

    //chatting client side
    private lateinit var clientSocket: Socket
    private lateinit var clientWriter: DataOutputStream

    /** Socket User Initializing Functions*/

    private fun initializeUser(writer: DataOutputStream) {
        val userUniqueId = runBlocking { dataStorePreferenceRepository.getUniqueDeviceId.first() }
        val userUniqueName = runBlocking { dataStorePreferenceRepository.getUsername.first() }
        val initialChatModel = InitialUserModel(
            userUniqueId = userUniqueId,
            userUniqueName = userUniqueName
        )
        val initialChatModelStringForm = gson.toJson(initialChatModel)
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
            handleEvents(
                TcpScreenEvents.OnDialogErrorOccurred(
                    TcpScreenDialogErrors.IOException
                )
            )
            try {
                writer.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    fun updateAllUsersOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            messagesRepository.updateAllUsersOnlineStatus(isOnline = isOnline)
        }
    }

    private fun setupUserData(reader: DataInputStream) {
        val receivedMessage = reader.readUTF()
        log("setup user data - $receivedMessage")

        val initialChattingUserModel = gson.fromJson(
            receivedMessage,
            InitialUserModel::class.java
        )

        // Call the function that handles user insertion and online status
        handleUserInsertionAndStatus(initialChattingUserModel)
    }

    // Function to handle both inserting the user and updating their online status
    private fun handleUserInsertionAndStatus(initialChatModel: InitialUserModel) {
        updateInitialChatModel(initialChatModel)
        viewModelScope.launch(Dispatchers.IO) {

            val userExists = messagesRepository.isUserExist(initialChatModel.userUniqueId)
            if (userExists) {
                messagesRepository.updateChattingUserUniqueName(
                    userUniqueId = initialChatModel.userUniqueId,
                    userUniqueName = initialChatModel.userUniqueName
                )
                messagesRepository.updateIsUserOnline(
                    userUniqueId = initialChatModel.userUniqueId,
                    isOnline = true
                )
            } else {
                val avatarBackgroundColor = RandomColors().getColor()
                val chattingUserEntity = ChattingUserEntity(
                    userUniqueId = initialChatModel.userUniqueId,
                    userUniqueName = initialChatModel.userUniqueName,
                    avatarBackgroundColor = avatarBackgroundColor,
                    isOnline = true
                )
                messagesRepository.insertChattingUser(chattingUserEntity)
            }

        }
    }

    private fun updateUserOnlineStatus(userUniqueId: String, isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            messagesRepository.updateIsUserOnline(userUniqueId = userUniqueId, isOnline = isOnline)
        }
    }

    private fun createServer(portNumber: Int) {
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
                        log("incoming message type - $messageType ")

                        when (messageType) {
                            AppMessageType.INITIAL -> {
                                setupUserData(reader = reader)
                            }

                            AppMessageType.VOICE -> {
                                viewModelScope.launch(Dispatchers.IO) {
                                    receiveVoiceMessage(reader = reader)
                                }
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
                                receiveFile(reader = reader)
                            }

                            AppMessageType.UNKNOWN -> {
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
                    }catch (e: Exception){
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
            closerServeSocket()
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
            closerServeSocket()
            updateHostConnectionStatus(HostConnectionStatus.Failure)
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.SecurityException)
            updateUserOnlineStatus(
                userUniqueId = state.value.peerUserUniqueId,
                isOnline = false
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
            log("createServer: IllegalArgumentException ")
            closerServeSocket()
            updateHostConnectionStatus(HostConnectionStatus.Failure)
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.IllegalArgumentException)
            updateUserOnlineStatus(
                userUniqueId = state.value.peerUserUniqueId,
                isOnline = false
            )
        }catch (e: Exception){
            e.printStackTrace()
            log("createServer: unknown exception ")
            closerServeSocket()
            updateHostConnectionStatus(HostConnectionStatus.Failure)
            updateHasErrorOccurredDialog(TcpScreenDialogErrors.UnknownException)
        }
    }

    // todo - check stream closes also
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
                closerServeSocket()
                updateHostConnectionStatus(HostConnectionStatus.Idle)
                updateUserOnlineStatus(
                    userUniqueId = state.value.peerUserUniqueId,
                    isOnline = false
                )
            }
        }
    }

    private fun closerServeSocket() {
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

    private fun connectToServer(serverIpAddress: String, serverPort: Int) {
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
                    log("incoming message type - $messageType ")

                    when (messageType) {
                        AppMessageType.INITIAL -> {
                            setupUserData(reader = reader)
                        }

                        AppMessageType.VOICE -> {
                            viewModelScope.launch(Dispatchers.IO) {
                                receiveVoiceMessage(reader = reader)
                            }
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
                            receiveFile(reader = reader)
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
            //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
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
                    viewModelScope.launch(Dispatchers.IO) {
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
            handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
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
            handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
        }
    }

    /**Socket Sending Functions*/

    private fun sendTextMessage(
        writer: DataOutputStream,
        textMessage: ChatMessageEntity
    ) {
        log("sending text message from client - $textMessage")
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
            handleEvents(
                TcpScreenEvents.OnDialogErrorOccurred(
                    TcpScreenDialogErrors.IOException
                )
            )
            try {
                writer.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    //fixme
    private suspend fun sendVoiceMessage(
        writer: DataOutputStream,
        voiceMessage: ChatMessageEntity
    ) {
        log("sending voice message ...")
        withContext(Dispatchers.IO) {
//        try {
            //sending file type
            val type = voiceMessage.type.identifier.code
            writer.writeChar(type)

            //sending file name
            writer.writeUTF(voiceMessage.voiceMessageFileName)
            log("sending voice file name - ${voiceMessage.voiceMessageFileName}")

            //Create File object
            val file = File(resourceDirectory, voiceMessage.voiceMessageFileName!!)
            val messageId = insertMessage(voiceMessage)

            //write length
            writer.writeLong(file.length())
            log("sending voice file length - ${file.length()}")

            var bytes: Int
            var bytesForPercentage = 0L
            val fileSizeForPercentage = file.length()
            val fileInputStream = FileInputStream(file)

            // Here we  break file into chunks
            val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)
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
                    withContext(Dispatchers.Main) {
                        log("progress - $percentage")
                        val newState = FileMessageState.Loading(percentage)
                        val newVoiceMessage =
                            voiceMessage.copy(fileState = newState, id = messageId)
                        updatePercentageOfReceivingFile(newVoiceMessage)
                    }
                }
            }
            // close the file here
            fileInputStream.close()

            withContext(Dispatchers.Main) {
                val newState = FileMessageState.Success
                val newVoiceMessage = voiceMessage.copy(fileState = newState, id = messageId)
                updatePercentageOfReceivingFile(newVoiceMessage)
                log("file sent successfully")
            }
//        } catch (exception: IOException) {
//            log("file sent failed")
//            val newState = FileMessageState.Failure
//            val newVoiceMessage = voiceMessage.copy(fileState = newState)
//            viewModel.updatePercentageOfReceivingFile(newVoiceMessage)
//        } catch (error: Exception) {
//            log("file sent failed")
//            val newState = FileMessageState.Failure
//            val newVoiceMessage = voiceMessage.copy(fileState = newState)
//            viewModel.updatePercentageOfReceivingFile(newVoiceMessage)
//        }
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
        val contactsStringForm = gson.toJson(contactsMessageItem)

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
            handleEvents(
                TcpScreenEvents.OnDialogErrorOccurred(
                    TcpScreenDialogErrors.IOException
                )
            )
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
    ) {
        log("sending file ...")

        withContext(Dispatchers.IO) {

            //sending file type
            val type = AppMessageType.FILE.identifier.code
            writer.writeChar(type)
            log("send file message - $type")

            //sending file count
            writer.writeInt(messages.size)
            log("file count - ${messages.size}")

            messages.forEach { fileMessage ->

                val messageId = insertMessage(fileMessage)
                log("message id - $messageId")

                try {

                    if (!resourceDirectory.exists()) {
                        resourceDirectory.mkdir()
                    }

                    val file = File(resourceDirectory, fileMessage.fileName!!)
                    log("sending file info: file size - ${file.length()} - ${file.name}")

                    //sending file name
                    writer.writeUTF(fileMessage.fileName)
                    log("sending file name - ${fileMessage.fileName}")

                    //write length
                    val fileSizeForPercentage = file.length()
                    writer.writeLong(file.length())
                    log("sending file length - ${file.length()}")

                    var bytes: Int
                    var bytesForPercentage = 0L
                    val fileInputStream = FileInputStream(file)

                    // Here we  break file into chunks
                    val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)
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
                            val newState = FileMessageState.Loading(percentage)
                            val newFileMessage =
                                fileMessage.copy(fileState = newState, id = messageId)
                            updatePercentageOfReceivingFile(newFileMessage)
                        }
                    }

                    // close the file here
                    fileInputStream.close()

                    val newState = FileMessageState.Success
                    val newFileMessage = fileMessage.copy(fileState = newState, id = messageId)
                    updatePercentageOfReceivingFile(newFileMessage)
                    log("file sent successfully")

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
                }
            }
        }
    }

    /** Socket Receiving Functions*/

    /**
     * 1. type
     * 2. file count
     * 3. file name
     * 4. file length
     * */
    private fun receiveFile(reader: DataInputStream) {
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

                var bytes = 0
                var bytesForPercentage = 0L
                val fileSizeForPercentage = fileSize
                val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)

                // Create File object
                val file = getFileByName(fileName = filename, resourceDirectory = resourceDirectory)

                // Create FileOutputStream to write the received file
                val fileMessageEntity = ChatMessageEntity(
                    type = AppMessageType.FILE,
                    formattedTime = getCurrentTime(),
                    isFromYou = false,
                    peerUniqueId = state.value.peerUserUniqueId,
                    authorUniqueId = state.value.authorUniqueId,
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

                            if (percentage != tempPercentage) {
                                log("progress - $percentage")
                                val newState = FileMessageState.Loading(percentage)
                                val newFileMessage =
                                    fileMessageEntity.copy(fileState = newState, id = messageId)
                                updatePercentageOfReceivingFile(newFileMessage)
                            }
                        }

                        log("file received successfully")
                        val newState = FileMessageState.Success
                        val newFileMessage =
                            fileMessageEntity.copy(fileState = newState, id = messageId)
                        updatePercentageOfReceivingFile(newFileMessage)

                    } catch (e: IOException) {
                        e.printStackTrace()
                        log("file receiving failed: ${e.message}")
                        val newState = FileMessageState.Failure
                        val newFileMessage =
                            fileMessageEntity.copy(fileState = newState, id = messageId)
                        updatePercentageOfReceivingFile(newFileMessage)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            log("file receiving process failed: ${e.message}")
        }
    }


    private fun receiveVoiceMessage(reader: DataInputStream) {
        log("receiving voice file ...")

        //reading file name
        val fileName = reader.readUTF()
        log("Expected voice file name - $fileName")

        // Read the expected file size
        var fileSize: Long = reader.readLong() // read file size
        log("voice file size - $fileSize")

        var bytes = 0
        var bytesForPercentage = 0L
        val fileSizeForPercentage = fileSize

        //Create File object
        val file = getFileByName(fileName = fileName, resourceDirectory = resourceDirectory)

        // Create FileOutputStream to write the received file
        val fileOutputStream = FileOutputStream(file)

        val voiceMessageEntity = ChatMessageEntity(
            type = AppMessageType.VOICE,
            formattedTime = getCurrentTime(),
            isFromYou = false,
            peerUniqueId = state.value.peerUserUniqueId,
            authorUniqueId = state.value.authorUniqueId,
            //message specific fields
            fileState = FileMessageState.Loading(0),
            voiceMessageFileName = file.name,
            voiceMessageAudioFileDuration = file.getAudioFileDuration(),
        )

        val messageId = runBlocking(Dispatchers.IO) {
            insertMessage(voiceMessageEntity)
        }

        val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)
        while (fileSize > 0
            && (reader.read(
                buffer, 0,
                min(buffer.size.toDouble(), fileSize.toDouble()).toInt()
            ).also { bytes = it })
            != -1
        ) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes)
            fileSize -= bytes.toLong()

            bytesForPercentage += bytes.toLong()
            val percentage =
                (bytesForPercentage.toDouble() / fileSizeForPercentage.toDouble() * 100).toInt()
            val tempPercentage =
                ((bytesForPercentage - bytes.toLong()) / fileSizeForPercentage.toDouble() * 100).toInt()

            if (percentage != tempPercentage) {
                log("progress - $percentage")
                val newState = FileMessageState.Loading(percentage)
                val newVoiceMessage = voiceMessageEntity.copy(fileState = newState, id = messageId)
                updatePercentageOfReceivingFile(newVoiceMessage)
            }
        }

        fileOutputStream.close()
        log("voice file received successfully")

        val newState = FileMessageState.Success
        val newVoiceMessage = voiceMessageEntity.copy(
            fileState = newState,
            voiceMessageAudioFileDuration = file.getAudioFileDuration(),
            id = messageId
        )
        updatePercentageOfReceivingFile(newVoiceMessage)
    }

    private fun receiveTextMessage(reader: DataInputStream) {
        val receivedMessage = reader.readUTF()
        log("host incoming text message - $receivedMessage")

        val textMessageEntity = ChatMessageEntity(
            type = AppMessageType.TEXT,
            formattedTime = getCurrentTime(),
            isFromYou = false,
            peerUniqueId = state.value.peerUserUniqueId,
            authorUniqueId = state.value.authorUniqueId,
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
            gson.fromJson(
                receivedMessage,
                ContactMessageItem::class.java
            )

        val contactMessageEntity = ChatMessageEntity(
            type = AppMessageType.CONTACT,
            formattedTime = getCurrentTime(),
            isFromYou = false,
            peerUniqueId = state.value.peerUserUniqueId,
            authorUniqueId = state.value.authorUniqueId,
            contactName = contactMessageItem.contactName,
            contactNumber = contactMessageItem.contactNumber
        )
        viewModelScope.launch(Dispatchers.IO) {
            insertMessage(contactMessageEntity)
        }
    }

    /** Hotspot networking creation*/

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

    private fun startHotspotNetworking() {
        viewModelScope.launch(Dispatchers.IO) {
            if (permissionGuard.canCreateNetwork()) {
                if (ServerDefaults.canUseCustomConfig()) {
                    createGroup()
                } else {
                    //fixme clarify error
                    updateHasErrorOccurredDialog(TcpScreenDialogErrors.AndroidVersion10RequiredForGroupNetworking)
                }
            } else {
                log("Permissions not granted for location!")
                // request at leas one time location permission,
                // this make requestPermissionForRationale return true
                emitNavigation(TcpScreenNavigation.RequestLocationPermission)
            }
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
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Unable to create Wifi Direct Group - ${r.displayReason}")
                //todo - show dialog error message with corresponding reason
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
                permissionGuard.requiredPermissionsForWifi.forEach { permission ->
                    onPermissionResult(
                        permission = permission,
                        isGranted = false
                    )
                }
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
        loadChattingUsers()
    }

    private fun initializeUserUniqueName() {
        viewModelScope.launch(Dispatchers.IO) {
            val authorUniqueId = dataStorePreferenceRepository.getUniqueDeviceId.first()
            _state.update {
                it.copy(
                    authorUniqueId = authorUniqueId
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
        }
    }

    private fun updateInitialChatModel(initialChatModel: InitialUserModel) {
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

    private fun loadChattingUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            messagesRepository.getAllUsersWithLastMessages().collect { users ->
                _state.update {
                    it.copy(
                        chattingUsers = Resource.Success(users.map { user -> user.toChattingUser() })
                    )
                }
            }
        }
    }

    fun loadMessages(chattingUser: ChattingUser) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    messages = Pager(
                        PagingConfig(pageSize = 18, prefetchDistance = 25),
                        pagingSourceFactory = {
                            messagesRepository.getPagedUserMessagesById(
                                chattingUser.userUniqueId
                            )
                        }
                    ).flow.mapNotNull { value: PagingData<ChatMessageEntity> ->
                        value.map { chatMessageEntity ->
                            chatMessageEntity.toChatMessage(chattingUser.username)!!
                        }
                    }.cachedIn(viewModelScope)
                )
            }
        }
    }

    fun checkReadContactsPermission() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isReadContactsGranted = permissionGuard.canAccessContacts()
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
                log("on wifi state changed - $networkEvent")
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
            peerUniqueId = state.value.peerUserUniqueId,
            authorUniqueId = state.value.authorUniqueId,
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
        if (currentAudioFile.exists()) {
            val isDeleted = currentAudioFile.delete()
            log("is cancelled audio deleted - $isDeleted")
        } else {
            log("file does not exist but cancel record called")
        }
    }

    private fun updateIsPlaying(audioState: AudioState, messageId: Long) {
        // Find the target message
//        val targetMessage: ChatMessage.VoiceMessage? = state.value.messages.collect{msg ->
//            msg.
//        }
//            .find { it.messageId == messageId && it.messageType == AppMessageType.VOICE } as? ChatMessage.VoiceMessage
//        // Check if targetMessage is not null
//        targetMessage?.let { message ->
//            // Create a copy with the updated isPlaying value
//            val updatedMessage = message.copy(audioState = audioState)
//            // Create a new list with the updated message
//            val newMessages = state.value.messages.map { msg ->
//                if (msg.messageId == messageId && msg.messageType == AppMessageType.VOICE) {
//                    updatedMessage
//                } else if (msg.messageType == AppMessageType.VOICE) {
//                    (msg as ChatMessage.VoiceMessage).copy(audioState = AudioState.Idle)
//                } else {
//                    msg
//                }
//            }
//            // Update the state with the new list of messages
//            _state.update { currentState ->
//                currentState.copy(
//                    messages = newMessages
//                )
//            }
//        }
    }

    private fun updatePlayTiming(timing: Long, messageId: Long) {
        // Find the target message
//        val targetMessage: ChatMessage.VoiceMessage? = state.value.messages
//            .find { it.messageId == messageId && it.messageType == AppMessageType.VOICE } as? ChatMessage.VoiceMessage
//        // Check if targetMessage is not null
//        targetMessage?.let {
//            val newAudioState = AudioState.Playing(timing)
//            // Create a copy with the updated isPlaying value
//            val updatedMessage = it.copy(audioState = newAudioState)
//            // Get the current list of messages
//            val messages = state.value.messages
//            // Find the index of the target message
//            val targetMessageIndex = messages.indexOf(targetMessage)
//            // Create a new list with the updated message
//            val newMessages = messages.toMutableList().apply {
//                set(targetMessageIndex, updatedMessage)
//            }
//            // Update the state with the new list of messages
//            _state.update { currentState ->
//                currentState.copy(
//                    messages = newMessages
//                )
//            }
//        }
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
            if (permissionGuard.canCreateLocalOnlyHotSpotNetwork()) {
                startLocalOnlyHotspot()
            } else {
                permissionGuard.requiredPermissionsForLocalOnlyHotSpot.forEach {
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

                    _state.update {
                        it.copy(
                            localOnlyHotspotName = config?.SSID ?: "",
                            localOnlyHotspotPassword = config?.preSharedKey ?: ""
                        )
                    }

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

    fun getCurrentChattingUser(currentChattingUser: ChattingUser) {
        viewModelScope.launch {
            messagesRepository.getChattingUserByIdFlow(currentChattingUser.userUniqueId)
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
    }

    @SuppressLint("NewApi")
    fun handleEvents(event: TcpScreenEvents) {
        when (event) {
            //todo - you did not use the parameter inside on play voice message
            is TcpScreenEvents.OnPlayVoiceMessageClick -> {
                playAudioFile(event.message)
            }

            is TcpScreenEvents.TcpChatItemClicked -> {
                emitNavigation(TcpScreenNavigation.OnChattingUserClicked(gson.toJson(event.currentChattingUser)))
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
                connectToWifi(event.wifiDevice)
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
                    peerUniqueId = state.value.peerUserUniqueId,
                    authorUniqueId = state.value.authorUniqueId,
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
                        //todo check that
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

    private suspend fun handlePickingMultipleMedia(medias: List<Uri>) {
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
                        resourceDirectory = resourceDirectory
                    )

                    val fileMessageEntity = ChatMessageEntity(
                        type = AppMessageType.FILE,
                        formattedTime = getCurrentTime(),
                        isFromYou = true,
                        peerUniqueId = state.value.peerUserUniqueId,
                        authorUniqueId = state.value.authorUniqueId,

                        fileState = FileMessageState.Loading(0),
                        fileName = file.name,
                        fileSize = file.length().readableFileSize(),
                        fileExtension = file.extension,
                        filePath = file.path,
                    )
                    fileMessages.add(fileMessageEntity)
                }

                viewModelScope.launch {
                    sendFileMessages(
                        writer = connectedClientWriter,
                        messages = fileMessages
                    )
                }
            }

            GeneralConnectionStatus.ConnectedAsClient -> {
                val fileMessages = mutableListOf<ChatMessageEntity>()
                medias.forEach { imageUri ->
                    val file = generateFileFromUri(contentResolver, imageUri, resourceDirectory)
                    val fileMessageEntity = ChatMessageEntity(
                        type = AppMessageType.FILE,
                        formattedTime = getCurrentTime(),
                        isFromYou = true,
                        peerUniqueId = state.value.peerUserUniqueId,
                        authorUniqueId = state.value.authorUniqueId,

                        fileState = FileMessageState.Loading(0),
                        fileName = file.name,
                        fileSize = file.length().readableFileSize(),
                        fileExtension = file.extension,
                        filePath = file.path,
                    )
                    fileMessages.add(fileMessageEntity)
                }
                viewModelScope.launch {
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

    private fun updatePercentageOfReceivingFile(message: ChatMessageEntity) {
        when (message.type) {
            AppMessageType.FILE -> {
                viewModelScope.launch(Dispatchers.IO) {
                    messagesRepository.updateFileMessage(
                        messageId = message.id,
                        newFileState = message.fileState
                    )
                }
            }

            AppMessageType.VOICE -> {
                log("updating voice message - $message")
                viewModelScope.launch(Dispatchers.IO) {
                    messagesRepository.updateVoiceFileMessage(
                        messageId = message.id,
                        newFileState = message.fileState,
                        newDuration = message.voiceMessageAudioFileDuration
                    )
                }
            }

            else -> return
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

data class InitialUserModel(
    val userUniqueId: String,
    val userUniqueName: String,
)