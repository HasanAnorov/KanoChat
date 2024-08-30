package com.ierusalem.androchat.features_local

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.utils.Json.gson
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.features_local.tcp.domain.InitialChatModel
import com.ierusalem.androchat.features_local.tcp.domain.state.ClientConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.HostConnectionStatus
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenDialogErrors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import javax.inject.Inject

@AndroidEntryPoint
class TcpService : Service() {

    @Inject
    lateinit var dataStorePreferenceRepository: DataStorePreferenceRepository

    //chatting server side
    private lateinit var serverSocket: ServerSocket
    private lateinit var connectedClientSocket: Socket
    private lateinit var connectedClientWriter: DataOutputStream

    //chatting client side
    private lateinit var clientSocket: Socket
    private lateinit var clientWriter: DataOutputStream

    // Binder given to clients.
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): TcpService = this@TcpService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**Socket connection setup*/

    fun createServer(
        serverPort: Int,
        createNewUserIfDoNotExist: (InitialChatModel) -> Unit,
        updateHostConnectionStatus: (HostConnectionStatus) -> Unit,
        updateConnectionsCount: (Boolean) -> Unit,
        onDialogErrorOccurred: (TcpScreenDialogErrors) -> Unit
    ) {
        log("creating server ...")
        updateHostConnectionStatus(HostConnectionStatus.Creating)

//            try {
        serverSocket = ServerSocket(serverPort)
        log("server created in : $serverSocket ${serverSocket.localSocketAddress}")
        if (serverSocket.isBound) {
            updateHostConnectionStatus(HostConnectionStatus.Created)
            updateConnectionsCount(true)
        }
        while (!serverSocket.isClosed) {
            connectedClientSocket = serverSocket.accept()
            connectedClientWriter = DataOutputStream(connectedClientSocket.getOutputStream())
            //here we sending the unique device id to the client
            initializeUser(
                writer = connectedClientWriter,
                onDialogErrorOccurred = onDialogErrorOccurred
            )
            log("New client : $connectedClientSocket ")
            updateConnectionsCount(true)

            while (!connectedClientSocket.isClosed) {
                val reader =
                    DataInputStream(BufferedInputStream(connectedClientSocket.getInputStream()))

//                        try {
                val messageType = AppMessageType.fromChar(reader.readChar())

                when (messageType) {
                    AppMessageType.INITIAL -> {
                        setupUserData(
                            reader = reader,
                            createNewUserIfDoNotExist = createNewUserIfDoNotExist
                        )
                    }

                    AppMessageType.VOICE -> {
                        //receiveVoiceMessage(reader = reader)
                    }

                    AppMessageType.CONTACT -> {
                        //receiveContactMessage(reader = reader)
                    }

                    AppMessageType.TEXT -> {
                        //receiveTextMessage(reader = reader)
                    }

                    AppMessageType.FILE -> {
                        //receiveFile(reader = reader)
                    }

                    AppMessageType.UNKNOWN -> {
                        /**Ignore case*/
                    }
                }
//                        } catch (e: EOFException) {
//                            e.printStackTrace()
//                            //if the IP address of the host could not be determined.
//                            log("createServer: EOFException")
//                            connectedClientSocketOnServer.close()
//                            viewModel.updateConnectionsCount(false)
//                            log("in while - ${connectedClientSocketOnServer.isClosed} - $connectedClientSocketOnServer")
//                            viewModel.handleEvents(
//                                TcpScreenEvents.OnDialogErrorOccurred(
//                                    TcpScreenDialogErrors.EOException
//                                )
//                            )
//                            try {
//                                reader.close()
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                            //the stream has been closed and the contained
//                            // input stream does not support reading after close,
//                            // or another I/O error occurs
//                            log("createServer: io exception ")
//                            viewModel.handleEvents(
//                                TcpScreenEvents.OnDialogErrorOccurred(
//                                    TcpScreenDialogErrors.IOException
//                                )
//                            )
//                            viewModel.updateConnectionsCount(false)
//                            connectedClientSocketOnServer.close()
//                            //serverSocket.close()
//                            try {
//                                reader.close()
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                                log("reader close exception - $e ")
//                            }
//                        } catch (e: UTFDataFormatException) {
//                            e.printStackTrace()
//                            /** here is firing***/
//                            //if the bytes do not represent a valid modified UTF-8 encoding of a string.
//                            log("createServer: io exception ")
//                            viewModel.handleEvents(
//                                TcpScreenEvents.OnDialogErrorOccurred(
//                                    TcpScreenDialogErrors.UTFDataFormatException
//                                )
//                            )
//                            viewModel.updateConnectionsCount(false)
//                            connectedClientSocketOnServer.close()
//                            //serverSocket.close()
//                            try {
//                                reader.close()
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                        }
            }
        }
//            } catch (e: IOException) {
//                e.printStackTrace()
//                serverSocket.close()
//                //change server title status
//                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)
//
//            } catch (e: SecurityException) {
//                e.printStackTrace()
//                serverSocket.close()
//                //change server title status
//                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)
//
//                //if a security manager exists and its checkConnect method doesn't allow the operation.
//                log("createServer: SecurityException ")
//            } catch (e: IllegalArgumentException) {
//                e.printStackTrace()
//                serverSocket.close()
//                //change server title status
//                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)
//                //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
//                log("createServer: IllegalArgumentException ")
//            }
    }

    fun connectToServer(
        serverIpAddress: String,
        serverPort: Int,
        updateClientConnectionStatus: (ClientConnectionStatus) -> Unit,
        updateConnectionsCount: (Boolean) -> Unit,
        onDialogErrorOccurred: (TcpScreenDialogErrors) -> Unit
    ) {
        log("connecting to server - $serverIpAddress:$serverPort")

        updateClientConnectionStatus(ClientConnectionStatus.Connecting)

        try {
            //create client socket
            clientSocket = Socket(serverIpAddress, serverPort)
            clientWriter = DataOutputStream(clientSocket.getOutputStream())
            log("client writer initialized - $clientWriter")

            initializeUser(
                writer = clientWriter,
                onDialogErrorOccurred = onDialogErrorOccurred
            )
            log("user initialized ")

            updateClientConnectionStatus(ClientConnectionStatus.Connected)
            updateConnectionsCount(true)

            //received outcome messages here
            while (!clientSocket.isClosed) {
                val reader = DataInputStream(BufferedInputStream(clientSocket.getInputStream()))

//                try {
                val dataType = AppMessageType.fromChar(reader.readChar())
                log("incoming message type - $dataType")

                when (dataType) {
                    AppMessageType.INITIAL -> {
                        setupUserData(reader = reader, createNewUserIfDoNotExist = {})
                    }

                    AppMessageType.VOICE -> {
//                        receiveVoiceMessage(reader = reader)
                    }

                    AppMessageType.TEXT -> {
//                        receiveTextMessage(reader = reader)
                    }

                    AppMessageType.CONTACT -> {
//                        receiveContactMessage(reader = reader)
                    }

                    AppMessageType.FILE -> {
//                        receiveFile(reader = reader)
                    }

                    AppMessageType.UNKNOWN -> {
                        /**Ignore case*/
                    }
                }
//                } catch (e: EOFException) {
//                    e.printStackTrace()
//                    //if the IP address of the host could not be determined.
//                    log("connectToServer: EOFException ")
//                    viewModel.handleEvents(
//                        TcpScreenEvents.OnDialogErrorOccurred(
//                            TcpScreenDialogErrors.EOException
//                        )
//                    )
//
//                    try {
//                        reader.close()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    //the stream has been closed and the contained
//                    // input stream does not support reading after close,
//                    // or another I/O error occurs
//                    log("connectToServer: io exception ")
//                    viewModel.updateClientConnectionStatus(ClientConnectionStatus.Failure)
//                    viewModel.updateConnectionsCount(false)
//                    viewModel.handleEvents(
//                        TcpScreenEvents.OnDialogErrorOccurred(
//                            TcpScreenDialogErrors.IOException
//                        )
//                    )
//                    try {
//                        reader.close()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                } catch (e: UTFDataFormatException) {
//                    e.printStackTrace()
//                    //if the bytes do not represent a valid modified UTF-8 encoding of a string.
//                    log("connectToServer: io exception ")
//                    viewModel.handleEvents(
//                        TcpScreenEvents.OnDialogErrorOccurred(
//                            TcpScreenDialogErrors.UTFDataFormatException
//                        )
//                    )
//                    try {
//                        reader.close()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
            }
        } catch (exception: UnknownHostException) {
            exception.printStackTrace()
            log("unknown host exception".uppercase())
            onDialogErrorOccurred(TcpScreenDialogErrors.UnknownHostException)
        } catch (exception: IOException) {
            exception.printStackTrace()
            //could not connect to a server
            log("connectToServer: IOException ".uppercase())
            onDialogErrorOccurred(TcpScreenDialogErrors.IOException)
        } catch (e: SecurityException) {
            e.printStackTrace()
            //fixme add security exception handling
            //if a security manager exists and its checkConnect method doesn't allow the operation.
            log("connectToServer: SecurityException".uppercase())
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            //fixme add illegal argument handling
            //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
            log("connectToServer: IllegalArgumentException".uppercase())
        }

    }

    /** Socket User Initializing Functions*/

    private fun initializeUser(
        writer: DataOutputStream,
        onDialogErrorOccurred: (TcpScreenDialogErrors) -> Unit
    ) {
        val userUniqueId =
            runBlocking(Dispatchers.IO) { dataStorePreferenceRepository.getUniqueDeviceId.first() }
        val username =
            runBlocking(Dispatchers.IO) { dataStorePreferenceRepository.getUsername.first() }
        val initialChatModel = InitialChatModel(
            userUniqueId = userUniqueId,
            userUniqueName = username
        )
        val initialChatModelStringForm = gson.toJson(initialChatModel)

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
            onDialogErrorOccurred(TcpScreenDialogErrors.IOException)
            try {
                writer.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    private fun setupUserData(
        reader: DataInputStream,
        createNewUserIfDoNotExist: (InitialChatModel) -> Unit
    ) {
        val receivedMessage = reader.readUTF()
        log("incoming initial message - $receivedMessage")

        val initialChatModel = gson.fromJson(
            receivedMessage,
            InitialChatModel::class.java
        )
        createNewUserIfDoNotExist(initialChatModel)
    }

    fun handleWifiDisabledCase(generalConnectionStatus: GeneralConnectionStatus) {
        when (generalConnectionStatus) {
            GeneralConnectionStatus.Idle -> {
                /** do nothing */
            }

            GeneralConnectionStatus.ConnectedAsClient -> {
                closeClientSocket()
            }

            GeneralConnectionStatus.ConnectedAsHost -> {
                closerServeSocket()
            }
        }
    }

    private fun closerServeSocket() {
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
    }

    private fun closeClientSocket() {
        if (::clientSocket.isInitialized) {
            clientSocket.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::connectedClientSocket.isInitialized) {
            connectedClientSocket.close()
        }
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
        if (::clientSocket.isInitialized) {
            clientSocket.close()
        }
    }

}

