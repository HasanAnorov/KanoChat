package com.ierusalem.androchat.features_local.tcp.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.ui.components.CoarseLocationPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.FineLocationPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.NearbyWifiDevicesPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.PermissionDialog
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.Constants.SOCKET_DEFAULT_BUFFER_SIZE
import com.ierusalem.androchat.core.utils.Constants.getCurrentTime
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.core.utils.getAudioFileDuration
import com.ierusalem.androchat.core.utils.getFileByName
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.openAppSettings
import com.ierusalem.androchat.core.utils.openWifiSettings
import com.ierusalem.androchat.core.utils.readableFileSize
import com.ierusalem.androchat.core.utils.shortToast
import com.ierusalem.androchat.features_local.TcpService
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_local.tcp.domain.state.ContactMessageItem
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenDialogErrors
import com.ierusalem.androchat.features_local.tcp.presentation.components.rememberTcpAllTabs
import com.ierusalem.androchat.features_local.tcp.presentation.utils.TcpScreenEvents
import com.ierusalem.androchat.features_local.tcp.presentation.utils.TcpScreenNavigation
import com.ierusalem.androchat.features_local.tcp.presentation.utils.TcpView
import com.ierusalem.androchat.features_local.tcp_conversation.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp_conversation.data.db.entity.FileMessageState
import com.ierusalem.androchat.features_local.tcp_networking.components.ActionRequestDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

@AndroidEntryPoint
class TcpFragment : Fragment() {

    private val viewModel: TcpViewModel by activityViewModels()

    //todo delegate this to viewmodel
    private lateinit var permissionGuard: PermissionGuardImpl

    //gson to convert message object to string
    private lateinit var gson: Gson

    //resource directory
    private lateinit var resourceDirectory: File

    private lateinit var tcpService: TcpService
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as TcpService.LocalBinder
            tcpService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService.
        Intent(requireContext(), TcpService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unbindService(connection)
        isBound = false
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                log("Precise location access granted: ")
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                log("Only approximate location access granted: ")
            }

            permissions.getOrDefault(Manifest.permission.NEARBY_WIFI_DEVICES, false) -> {
                // Only approximate location access granted.
                log("Only approximate location access granted: ")
            }

            else -> {
                // No location access granted.
                permissions.forEach { permission ->
                    viewModel.onPermissionResult(
                        permission = permission.key,
                        isGranted = permission.value
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gson = Gson()
        resourceDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}"
        )!!
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        permissionGuard = PermissionGuardImpl(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.state.collectAsStateWithLifecycle()

                val visibleActionDialogQueue = viewModel.visibleActionDialogQueue
                val visiblePermissionDialogQueue = viewModel.visiblePermissionDialogQueue

                val scope = rememberCoroutineScope()
                val allTabs = rememberTcpAllTabs()
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0F,
                    pageCount = { allTabs.size },
                )
                val focusManager = LocalFocusManager.current
                val handleTabSelected by rememberUpdatedState { tab: TcpView ->
                    // Click fires the index to update
                    // The index updating is caught by the snapshot flow
                    // Which then triggers the page update function
                    val index = allTabs.indexOf(tab)
                    scope.launch(context = Dispatchers.Main) {
                        pagerState.animateScrollToPage(
                            index
                        )
                    }
                }

                LaunchedEffect(key1 = pagerState.currentPage) {
                    focusManager.clearFocus()
                }

                AndroChatTheme {

                    visibleActionDialogQueue.forEach { actionDialog ->
                        ActionRequestDialog(
                            onDismissRequest = actionDialog.onNegativeButtonClick,
                            onConfirmation = actionDialog.onPositiveButtonClick,
                            dialogTitle = actionDialog.dialogTitle,
                            dialogText = actionDialog.dialogMessage,
                            icon = actionDialog.icon,
                            positiveButtonRes = actionDialog.positiveButtonText,
                            negativeButtonRes = actionDialog.negativeButtonText
                        )
                    }

                    visiblePermissionDialogQueue.reversed().forEach { permission ->
                        PermissionDialog(
                            permissionTextProvider = when (permission) {
                                Manifest.permission.NEARBY_WIFI_DEVICES -> {
                                    NearbyWifiDevicesPermissionTextProvider()
                                }

                                Manifest.permission.ACCESS_FINE_LOCATION -> {
                                    FineLocationPermissionTextProvider()
                                }

                                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                    CoarseLocationPermissionTextProvider()
                                }

                                else -> return@forEach
                            },
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                permission
                            ),
                            onDismiss = viewModel::dismissPermissionDialog,
                            onOkClick = {
                                when (permission) {

                                    Manifest.permission.NEARBY_WIFI_DEVICES -> {
                                        viewModel.dismissPermissionDialog()
                                        locationPermissionRequest.launch(
                                            permissionGuard.requiredPermissionsForWifi.toTypedArray()
                                        )
                                    }

                                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                                        viewModel.dismissPermissionDialog()
                                        locationPermissionRequest.launch(
                                            permissionGuard.requiredPermissionsForWifi.toTypedArray()
                                        )
                                    }

                                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                        viewModel.dismissPermissionDialog()
                                        locationPermissionRequest.launch(
                                            permissionGuard.requiredPermissionsForWifi.toTypedArray()
                                        )
                                    }
                                }
                            },
                            onGoToAppSettingsClick = {
                                openAppSettings()
                                viewModel.dismissPermissionDialog()
                            }
                        )
                    }

                    TcpScreen(
                        uiState = uiState,
                        eventHandler = viewModel::handleEvents,
                        allTabs = allTabs,
                        pagerState = pagerState,
                        onTabChanged = { handleTabSelected(it) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.screenNavigation.executeWithLifecycle(
            lifecycle = viewLifecycleOwner.lifecycle,
            action = ::executeNavigation
        )
    }

    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

            TcpScreenNavigation.WifiEnableRequest -> {
                openWifiSettings()
            }

            TcpScreenNavigation.OnChattingUserClicked -> {
                findNavController().navigate(R.id.action_tcpFragment_to_localConversationFragment)
            }

            TcpScreenNavigation.OnSettingsClick -> {
                findNavController().navigate(R.id.action_tcpFragment_to_settingsFragment)
            }

            TcpScreenNavigation.WifiDisabledCase -> {
                tcpService.handleWifiDisabledCase(viewModel.state.value.generalConnectionStatus)
            }

            is TcpScreenNavigation.OnErrorsOccurred -> {
                shortToast(getString(navigation.tcpScreenErrors.errorMessage))
            }

            is TcpScreenNavigation.OnCreateServerClick -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    tcpService.createServer(
                        serverPort = navigation.portNumber,
                        onDialogErrorOccurred = {
                            viewModel.handleEvents(
                                TcpScreenEvents.OnDialogErrorOccurred(it)
                            )
                        },
                        createNewUserIfDoNotExist = {
                            viewModel.createNewUserIfDoNotExist(it)
                        },
                        updateConnectionsCount = {
                            viewModel.updateConnectionsCount(it)
                        },
                        updateHostConnectionStatus = {
                            viewModel.updateHostConnectionStatus(it)
                        }
                    )
                }
            }

            is TcpScreenNavigation.OnConnectToServerClick -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    tcpService.connectToServer(
                        serverIpAddress = navigation.serverIpAddress,
                        serverPort = navigation.portNumber,
                        onDialogErrorOccurred = {
                            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(it))
                        },
                        updateConnectionsCount = { viewModel.updateConnectionsCount(it) },
                        updateClientConnectionStatus = { viewModel.updateClientConnectionStatus(it) }
                    )
                }
            }

            is TcpScreenNavigation.SendHostMessage -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    //sendHostMessage(navigation.message)
                }
            }

            is TcpScreenNavigation.SendClientMessage -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    //sendClientMessage(navigation.message)
                }
            }
        }
    }

    /** Socket Receiving Functions */

    /***
     * 1. type
     * 2. file count
     * 3. file name
     * 4. file length
     * */
    private fun receiveFile(reader: DataInputStream) {
        log("receiving file ...")

        //reading file count
        val fileCount = reader.readInt()
        log("file count - $fileCount")

        for (i in 0 until fileCount) {
            //reading file name
            val filename = reader.readUTF()
            log("Expected file name - $filename")

            // Read the expected file size
            var fileSize: Long = reader.readLong() // read file size
            log("file size - $fileSize")

            var bytes = 0
            var bytesForPercentage = 0L
            val fileSizeForPercentage = fileSize

            // Create File object
            val file = getFileByName(fileName = filename, resourceDirectory = resourceDirectory)

            // Create FileOutputStream to write the received file
            val fileOutputStream = FileOutputStream(file)

            val fileMessageEntity = ChatMessageEntity(
                type = AppMessageType.FILE,
                formattedTime = getCurrentTime(),
                isFromYou = false,
                userId = viewModel.state.value.peerUserUniqueId,
                //file specific fields
                fileState = FileMessageState.Loading(0),
                fileName = file.name,
                fileSize = fileSize.readableFileSize(),
                fileExtension = file.extension,
                filePath = file.path,
            )

            val messageId = runBlocking(Dispatchers.IO) {
                viewModel.insertMessage(fileMessageEntity)
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
                    val newFileMessage =
                        fileMessageEntity.copy(fileState = newState, id = messageId)
                    viewModel.updatePercentageOfReceivingFile(newFileMessage)
                }
            }

            fileOutputStream.close()
            log("file received successfully")

            val newState = FileMessageState.Success
            val newFileMessage = fileMessageEntity.copy(fileState = newState, id = messageId)

            viewModel.updatePercentageOfReceivingFile(newFileMessage)
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
            userId = viewModel.state.value.peerUserUniqueId,
            //message specific fields
            fileState = FileMessageState.Loading(0),
            voiceMessageFileName = file.name,
            voiceMessageAudioFileDuration = file.getAudioFileDuration(),
        )

        val messageId = runBlocking(Dispatchers.IO) {
            viewModel.insertMessage(voiceMessageEntity)
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
                viewModel.updatePercentageOfReceivingFile(newVoiceMessage)
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
        viewModel.updatePercentageOfReceivingFile(newVoiceMessage)
    }

    private fun receiveTextMessage(reader: DataInputStream) {
        val receivedMessage = reader.readUTF()
        log("host incoming text message - $receivedMessage")

        val textMessageEntity = ChatMessageEntity(
            type = AppMessageType.TEXT,
            formattedTime = getCurrentTime(),
            isFromYou = false,
            userId = viewModel.state.value.peerUserUniqueId,
            text = receivedMessage.toString()
        )
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.insertMessage(textMessageEntity)
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
            userId = viewModel.state.value.peerUserUniqueId,
            contactName = contactMessageItem.contactName,
            contactNumber = contactMessageItem.contactNumber
        )
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.insertMessage(contactMessageEntity)
        }
    }

    /**Socket Sending Functions*/

//    private fun sendClientMessage(message: ChatMessageEntity) {
//        if (!clientSocket.isClosed) {
//            when (message.type) {
//                AppMessageType.TEXT -> {
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        sendTextMessage(writer = clientWriter, textMessage = message)
//                    }
//                }
//
//                AppMessageType.VOICE -> {
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        sendVoiceMessage(writer = clientWriter, voiceMessage = message)
//                    }
//                }
//
//                AppMessageType.CONTACT -> {
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        sendContactMessage(writer = clientWriter, contactMessage = message)
//                    }
//                }
//
//                AppMessageType.FILE -> {
//                    lifecycleScope.launch {
//                        sendFileMessages(writer = clientWriter, messages = listOf(message))
//                    }
//                }
//
//                else -> {
//                    /** Just ignore */
//                }
//            }
//        } else {
//            log("send client message: client socket is closed ")
//            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
//        }
//    }
//
//    private fun sendHostMessage(message: ChatMessageEntity) {
//        if (!connectedClientSocketOnServer.isClosed) {
//            when (message.type) {
//                AppMessageType.TEXT -> {
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        sendTextMessage(writer = connectedClientWriter, textMessage = message)
//                    }
//                }
//
//                AppMessageType.VOICE -> {
//                    lifecycleScope.launch {
//                        sendVoiceMessage(writer = connectedClientWriter, voiceMessage = message)
//                    }
//                }
//
//                AppMessageType.CONTACT -> {
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        sendContactMessage(writer = connectedClientWriter, contactMessage = message)
//                    }
//                }
//
//                AppMessageType.FILE -> {
//                    lifecycleScope.launch {
//                        sendFileMessages(writer = connectedClientWriter, messages = listOf(message))
//                    }
//                }
//
//                else -> {
//                    /** Just ignore */
//                }
//            }
//        } else {
//            log("send host message: client socket is closed ")
//            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
//        }
//    }

    /**Socket Sending Functions*/

    private fun sendTextMessage(
        writer: DataOutputStream,
        textMessage: ChatMessageEntity
    ) {
        log("sending text message from client - $textMessage")
        try {
            writer.writeChar(textMessage.type.identifier.code)
            writer.writeUTF(textMessage.text)
            lifecycleScope.launch {
                viewModel.insertMessage(textMessage)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(
                "ahi3646",
                "sendMessage server: dataOutputStream is closed io exception "
            )
            viewModel.handleEvents(
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
            val messageId = viewModel.insertMessage(voiceMessage)

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
                        viewModel.updatePercentageOfReceivingFile(newVoiceMessage)
                    }
                }
            }
            // close the file here
            fileInputStream.close()

            withContext(Dispatchers.Main) {
                val newState = FileMessageState.Success
                val newVoiceMessage = voiceMessage.copy(fileState = newState, id = messageId)
                viewModel.updatePercentageOfReceivingFile(newVoiceMessage)
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
            lifecycleScope.launch {
                viewModel.insertMessage(contactMessage)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(
                "ahi3646",
                "sendMessage server: dataOutputStream is closed io exception "
            )
            viewModel.handleEvents(
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
//                try {

            //sending file type
            val type = AppMessageType.FILE.identifier.code
            writer.writeChar(type)

            //sending file count
            writer.writeInt(messages.size)
            log("messages size - ${messages.size}")

            messages.forEach { fileMessage ->

                //todo-check for duplication
                val file = File(resourceDirectory, fileMessage.fileName!!)
                log("sending file info: file size - ${file.length()} - ${file.name}")

                val messageId = viewModel.insertMessage(fileMessage)
                log("message id - $messageId")

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
                        withContext(Dispatchers.Main) {
                            log("progress - $percentage")
                            val newState = FileMessageState.Loading(percentage)
                            val newFileMessage =
                                fileMessage.copy(fileState = newState, id = messageId)
                            viewModel.updatePercentageOfReceivingFile(newFileMessage)
                        }
                    }
                }

                // close the file here
                fileInputStream.close()

                withContext(Dispatchers.Main) {
                    val newState = FileMessageState.Success
                    val newFileMessage = fileMessage.copy(fileState = newState, id = messageId)
                    viewModel.updatePercentageOfReceivingFile(newFileMessage)
                    log("file sent successfully")
                }

//                } catch (exception: IOException) {
//                    exception.printStackTrace()
//                    withContext(Dispatchers.Main) {
//                        log("file sent failed")
//                        val newState = FileMessageState.Failure
//                        viewModel.updatePercentageOfReceivingFile(fileMessage, newState)
//                    }
//                } catch (error: Exception) {
//                    error.printStackTrace()
//                    withContext(Dispatchers.Main) {
//                        log("file sent failed")
//                        val newState = FileMessageState.Failure
//                        viewModel.updatePercentageOfReceivingFile(fileMessage, newState)
//                    }
//                }
            }
        }
    }

}