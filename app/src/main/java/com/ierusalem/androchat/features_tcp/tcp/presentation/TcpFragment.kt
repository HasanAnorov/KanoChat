package com.ierusalem.androchat.features_tcp.tcp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.app.BroadcastFrequency
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.core.constants.Constants.SOCKET_DEFAULT_BUFFER_SIZE
import com.ierusalem.androchat.core.constants.Constants.getCurrentTime
import com.ierusalem.androchat.core.ui.components.CoarseLocationPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.FineLocationPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.NearbyWifiDevicesPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.PermissionDialog
import com.ierusalem.androchat.core.ui.components.ReadContactsPermissionTextProvider
import com.ierusalem.androchat.core.ui.components.RecordAudioPermissionTextProvider
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.core.utils.generateFileFromUri
import com.ierusalem.androchat.core.utils.getAudioFileDuration
import com.ierusalem.androchat.core.utils.getFileByName
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.makeCall
import com.ierusalem.androchat.core.utils.openAppSettings
import com.ierusalem.androchat.core.utils.openFile
import com.ierusalem.androchat.core.utils.readableFileSize
import com.ierusalem.androchat.core.utils.shortToast
import com.ierusalem.androchat.features_tcp.server.ServerDefaults
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_tcp.server.wifidirect.Reason
import com.ierusalem.androchat.features_tcp.server.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_tcp.server.wifidirect.WiFiNetworkEvent
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ClientConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ContactMessageItem
import com.ierusalem.androchat.features_tcp.tcp.domain.state.GeneralConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.HostConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.HotspotNetworkingStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.P2PNetworkingStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenDialogErrors
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenErrors
import com.ierusalem.androchat.features_tcp.tcp.presentation.components.rememberTcpAllTabs
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpScreenNavigation
import com.ierusalem.androchat.features_tcp.tcp.presentation.utils.TcpView
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.FileMessageState
import com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components.ContactListContent
import com.ierusalem.androchat.features_tcp.tcp_networking.components.ActionRequestDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import kotlin.math.min

@AndroidEntryPoint
class TcpFragment : Fragment() {

    private val viewModel: TcpViewModel by viewModels()

    //wifi direct
    private lateinit var wifiP2PManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private val intentFilter = IntentFilter()

    //todo delegate this to viewmodel
    private lateinit var permissionGuard: PermissionGuardImpl

    //gson to convert message object to string
    private lateinit var gson: Gson

    //chatting server side
    private lateinit var serverSocket: ServerSocket
    private lateinit var connectedClientSocketOnServer: Socket
    private lateinit var connectedClientWriter: DataOutputStream

    //chatting client side
    private lateinit var clientSocket: Socket
    private lateinit var clientWriter: DataOutputStream

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

    private lateinit var resourceDirectory: File

    //1-fragment lifecycle callback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gson = Gson()
        resourceDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}"
        )!!
    }

    private val readContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onPermissionResult(Manifest.permission.READ_CONTACTS, isGranted)
        }

    private val recordAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onPermissionResult(Manifest.permission.RECORD_AUDIO, isGranted)
            if (isGranted) {
                Toast.makeText(
                    requireContext(),
                    R.string.ready_to_record_voice_message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val getFilesLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_CANCELED -> {
                log("onActivityResult: RESULT CANCELED ")
            }

            Activity.RESULT_OK -> {
                val intent: Intent = result.data!!
                val uri = intent.data!!
                val file = generateFileFromUri(uri, resourceDirectory)

                val fileMessageEntity = ChatMessageEntity(
                    type = AppMessageType.FILE,
                    formattedTime = getCurrentTime(),
                    isFromYou = true,
                    userId = viewModel.state.value.peerUserUniqueId,

                    filePath = file.path,
                    fileState = FileMessageState.Loading(0),
                    fileName = file.name,
                    fileSize = file.length().readableFileSize(),
                    fileExtension = file.extension,
                )

                when (viewModel.state.value.generalConnectionStatus) {
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
        }
    }

    private fun showFileChooser() {
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        try {
            getFilesLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_install_a_file_manager),
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun handlePickingMultipleMedia(medias: List<Uri>) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (viewModel.state.value.generalConnectionStatus) {
                GeneralConnectionStatus.Idle -> {
                    viewModel.updateHasErrorOccurredDialog(TcpScreenDialogErrors.EstablishConnectionToSendMessage)
                }

                GeneralConnectionStatus.ConnectedAsHost -> {
                    val fileMessages = mutableListOf<ChatMessageEntity>()
                    medias.forEach { imageUri ->
                        val file = generateFileFromUri(imageUri, resourceDirectory)

                        val fileMessageEntity = ChatMessageEntity(
                            type = AppMessageType.FILE,
                            formattedTime = getCurrentTime(),
                            isFromYou = true,
                            userId = viewModel.state.value.peerUserUniqueId,

                            fileState = FileMessageState.Loading(0),
                            fileName = file.name,
                            fileSize = file.length().readableFileSize(),
                            fileExtension = file.extension,
                            filePath = file.path,
                        )
                        fileMessages.add(fileMessageEntity)
                    }
                    lifecycleScope.launch {
                        sendFileMessages(
                            writer = connectedClientWriter,
                            messages = fileMessages
                        )
                    }
                }

                GeneralConnectionStatus.ConnectedAsClient -> {
                    val fileMessages = mutableListOf<ChatMessageEntity>()
                    medias.forEach { imageUri ->
                        val file = generateFileFromUri(imageUri, resourceDirectory)
                        val fileMessageEntity = ChatMessageEntity(
                            type = AppMessageType.FILE,
                            formattedTime = getCurrentTime(),
                            isFromYou = true,
                            userId = viewModel.state.value.peerUserUniqueId,

                            fileState = FileMessageState.Loading(0),
                            fileName = file.name,
                            fileSize = file.length().readableFileSize(),
                            fileExtension = file.extension,
                            filePath = file.path,
                        )
                        fileMessages.add(fileMessageEntity)
                    }
                    lifecycleScope.launch {
                        sendFileMessages(
                            writer = clientWriter,
                            messages = fileMessages
                        )
                    }
                }
            }
        }
    }

    //2-fragment lifecycle callback
    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        permissionGuard = PermissionGuardImpl(requireContext())
        wifiP2PManager =
            requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

        channel = wifiP2PManager.initialize(
            requireContext(), Looper.getMainLooper()
        ) {
            log("WifiP2PManager Channel died! Do nothing :D")
        }
        intentFilter.apply {
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        }

        return ComposeView(requireContext()).apply {
            setContent {
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

                val uiState by viewModel.state.collectAsStateWithLifecycle()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

                AndroChatTheme {
                    if (uiState.showBottomSheet) {
                        ModalBottomSheet(
                            sheetState = sheetState,
                            onDismissRequest = {
                                viewModel.handleEvents(TcpScreenEvents.UpdateBottomSheetState(false))
                            },
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            content = {
                                if (uiState.isReadContactsGranted) {
                                    viewModel.handleEvents(TcpScreenEvents.ReadContacts)
                                    ContactListContent(
                                        contacts = uiState.contacts,
                                        shareSelectedContacts = { selectedContacts ->
                                            when (uiState.generalConnectionStatus) {
                                                GeneralConnectionStatus.Idle -> {
                                                    //do nothing
                                                }

                                                GeneralConnectionStatus.ConnectedAsClient -> {
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        viewModel.handleEvents(
                                                            TcpScreenEvents.UpdateBottomSheetState(
                                                                false
                                                            )
                                                        )
                                                        selectedContacts.forEach { contact ->
                                                            val contactMessageEntity =
                                                                ChatMessageEntity(
                                                                    type = AppMessageType.CONTACT,
                                                                    formattedTime = getCurrentTime(),
                                                                    isFromYou = true,
                                                                    userId = viewModel.state.value.peerUserUniqueId,

                                                                    contactName = contact.contactName,
                                                                    contactNumber = contact.phoneNumber,
                                                                )
                                                            sendClientMessage(contactMessageEntity)
                                                            delay(300)
                                                        }
                                                    }
                                                }

                                                GeneralConnectionStatus.ConnectedAsHost -> {
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        viewModel.handleEvents(
                                                            TcpScreenEvents.UpdateBottomSheetState(
                                                                false
                                                            )
                                                        )
                                                        selectedContacts.forEach { contact ->
                                                            val contactMessageEntity =
                                                                ChatMessageEntity(
                                                                    type = AppMessageType.CONTACT,
                                                                    formattedTime = getCurrentTime(),
                                                                    isFromYou = true,
                                                                    userId = viewModel.state.value.peerUserUniqueId,

                                                                    contactName = contact.contactName,
                                                                    contactNumber = contact.phoneNumber,
                                                                )
                                                            sendHostMessage(contactMessageEntity)
                                                            delay(300)
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(300.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center,
                                        content = {
                                            Button(
                                                onClick = {
                                                    readContactsPermissionLauncher.launch(
                                                        Manifest.permission.READ_CONTACTS
                                                    )
                                                }
                                            ) {
                                                Text(text = stringResource(R.string.give_permission))
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }

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
                                Manifest.permission.READ_CONTACTS -> {
                                    ReadContactsPermissionTextProvider()
                                }

                                Manifest.permission.RECORD_AUDIO -> {
                                    RecordAudioPermissionTextProvider()
                                }

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
                                    Manifest.permission.READ_CONTACTS -> {
                                        viewModel.dismissPermissionDialog()
                                        readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                    }

                                    Manifest.permission.RECORD_AUDIO -> {
                                        viewModel.dismissPermissionDialog()
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }

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
                        state = uiState,
                        //try to use pass lambda like this,
                        // this will help to avoid extra recomposition
                        eventHandler = viewModel::handleEvents,
                        allTabs = allTabs,
                        pagerState = pagerState,
                        onTabChanged = { handleTabSelected(it) })
                }
            }
        }
    }

    //3-fragment lifecycle callback
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.screenNavigation.executeWithLifecycle(
            lifecycle = viewLifecycleOwner.lifecycle,
            action = ::executeNavigation
        )
    }

    // todo - check stream closes also
    // network clean up should be carried out in viewmodel
    private fun handleWifiDisabledCase() {
        when (viewModel.state.value.generalConnectionStatus) {
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

    @SuppressLint("MissingPermission", "NewApi")
    private fun createGroup() {
        viewModel.updateHotspotDiscoveryStatus(HotspotNetworkingStatus.LaunchingHotspot)
        val config = getConfiguration(
            viewModel.state.value.networkBand
        )
        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("New network created")
                viewModel.updateHotspotDiscoveryStatus(HotspotNetworkingStatus.HotspotRunning)
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Unable to create Wifi Direct Group - ${r.displayReason}")
                //todo - show dialog error message with corresponding reason
                viewModel.updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Failure)
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

    private fun getConfiguration(networkBand:BroadcastFrequency): WifiP2pConfig? {
        if (!ServerDefaults.canUseCustomConfig()) {
            return null
        }

        val ssid = ServerDefaults.asSsid(
            //here you have to return preferred ssid from data store or preference helper
            viewModel.state.value.hotspotName
        )
        //todo i will use manual password here
        //val passwd = generateRandomPassword(8)
        val passwd = "12345678"

        //here you have to return preferred wifi band like 2,4hz or 5hz
        val band = when(networkBand){
            BroadcastFrequency.FREQUENCY_2_4_GHZ -> {
                WifiP2pConfig.GROUP_OWNER_BAND_2GHZ
            }
            BroadcastFrequency.FREQUENCY_5_GHZ -> {
                WifiP2pConfig.GROUP_OWNER_BAND_5GHZ
            }
        }
        return WifiP2pConfig
            .Builder()
            .setNetworkName(ssid)
            .setPassphrase(passwd)
            .setGroupOperatingBand(band)
            .build()
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
                viewModel.updateConnectedDevices(wifiP2pDevice)
            }

            override fun onFailure(reason: Int) {
                log("failure: failure on wifi connection ")
                viewModel.emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.FailedToConnectToWifiDevice))
            }
        }
        wifiP2PManager.connect(channel, config, listener)
    }

    private fun startHotspotNetworking() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (permissionGuard.canCreateNetwork()) {
                if (ServerDefaults.canUseCustomConfig()) {
                    createGroup()
                } else {
                    //fixme clarify error
                    viewModel.updateHasErrorOccurredDialog(TcpScreenDialogErrors.AndroidVersion10RequiredForGroupNetworking)
                }
            } else {
                log("Permissions not granted!")
                locationPermissionRequest.launch(
                    permissionGuard.requiredPermissionsForWifi.toTypedArray()
                )
            }
        }
    }

    private fun stopHotspotNetworking() {
        //close socket only when serverSocket is initialized
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
        if (::connectedClientSocketOnServer.isInitialized) {
            connectedClientSocketOnServer.close()
        }

        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("Wifi P2P Channel is removed")
                viewModel.updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Idle)
                viewModel.handleNetworkEvents(
                    WiFiNetworkEvent.ConnectionStatusChanged(
                        GeneralConnectionStatus.Idle
                    )
                )
                viewModel.handleNetworkEvents(WiFiNetworkEvent.UpdateGroupOwnerAddress("Not connected"))
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Failed to stop network: ${r.displayReason}")
            }
        }
        wifiP2PManager.removeGroup(channel, listener)
    }

    @SuppressLint("MissingPermission")
    private fun discoverP2PConnection() {
        viewModel.updateP2PDiscoveryStatus(P2PNetworkingStatus.Discovering)
        lifecycleScope.launch(Dispatchers.IO) {
            if (permissionGuard.canCreateNetwork()) {
                val listener = object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        log("onSuccess: discover ")
                        viewModel.updateP2PDiscoveryStatus(P2PNetworkingStatus.Discovering)
                    }

                    override fun onFailure(reason: Int) {
                        // Code for when the discovery initiation fails goes here.
                        // Alert the user that something went wrong.
                        log("onFailure: discover $reason ")
                        viewModel.updateP2PDiscoveryStatus(P2PNetworkingStatus.Failure)
                    }
                }
                wifiP2PManager.discoverPeers(channel, listener)
            } else {
                log("Permissions not granted!")
                locationPermissionRequest.launch(
                    permissionGuard.requiredPermissionsForWifi.toTypedArray()
                )
            }
        }
    }

    private fun stopPeerDiscovery() {
        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("Wifi P2P Discovery is stopped")
                viewModel.updateP2PDiscoveryStatus(P2PNetworkingStatus.Idle)
                viewModel.clearPeersList()
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Failed to stop p2p discovery: ${r.displayReason}")
            }
        }
        wifiP2PManager.stopPeerDiscovery(channel, listener)
    }

    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

            TcpScreenNavigation.WifiEnableRequest -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }

            TcpScreenNavigation.RequestReadContactsPermission -> {
                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }

            TcpScreenNavigation.RequestRecordAudioPermission -> {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            is TcpScreenNavigation.HandlePickingMultipleMedia -> {
                handlePickingMultipleMedia(navigation.medias)
            }

            is TcpScreenNavigation.OnContactItemClick -> {
                makeCall(phoneNumber = navigation.message.contactNumber)
            }

            is TcpScreenNavigation.OnFileItemClick -> {
                openFile(
                    fileName = navigation.message.fileName,
                    resourceDirectory = resourceDirectory
                )
            }

            is TcpScreenNavigation.OnErrorsOccurred -> {
                shortToast(getString(navigation.tcpScreenErrors.errorMessage))
            }

            TcpScreenNavigation.ShowFileChooserClick -> {
                showFileChooser()
            }

            TcpScreenNavigation.WifiDisabledCase -> {
                handleWifiDisabledCase()
            }

            TcpScreenNavigation.OnDiscoverP2PClick -> {
                discoverP2PConnection()
            }

            TcpScreenNavigation.OnStopP2PDiscovery -> {
                stopPeerDiscovery()
            }

            TcpScreenNavigation.OnStartHotspotNetworking -> {
                startHotspotNetworking()
            }

            TcpScreenNavigation.OnStopHotspotNetworking -> {
                stopHotspotNetworking()
            }

            TcpScreenNavigation.OnNavIconClick -> {
                findNavController().popBackStack()
            }

            TcpScreenNavigation.OnSettingsClick -> {
                findNavController().navigate(R.id.action_tcpFragment_to_settingsFragment)
            }

            is TcpScreenNavigation.OnCreateServerClick -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    createServer(serverPort = navigation.portNumber)
                }
            }

            is TcpScreenNavigation.OnConnectToServerClick -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    connectToServer(
                        serverIpAddress = navigation.serverIpAddress,
                        serverPort = navigation.portNumber
                    )
                }
            }

            is TcpScreenNavigation.OnConnectToWifiClick -> {
                connectToWifi(navigation.wifiP2pDevice)
            }

            is TcpScreenNavigation.SendHostMessage -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    sendHostMessage(navigation.message)
                }
            }

            is TcpScreenNavigation.SendClientMessage -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    sendClientMessage(navigation.message)
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

    /**Socket connection setup*/

    private fun createServer(serverPort: Int) {
        log("creating server ...")
        log("group address - ${viewModel.state.value.groupOwnerAddress} \ncreating server ...")
        viewModel.updateHostConnectionStatus(HostConnectionStatus.Creating)

//            try {
        serverSocket = ServerSocket(serverPort)
        log("server created in : $serverSocket ${serverSocket.localSocketAddress}")
        if (serverSocket.isBound) {
            viewModel.updateHostConnectionStatus(HostConnectionStatus.Created)
            viewModel.updateConnectionsCount(true)
        }
        while (!serverSocket.isClosed) {
            connectedClientSocketOnServer = serverSocket.accept()
            connectedClientWriter =
                DataOutputStream(connectedClientSocketOnServer.getOutputStream())
            //here we sending the unique device id to the client
            initializeUser(writer = connectedClientWriter)
            log("New client : $connectedClientSocketOnServer ")
            viewModel.updateConnectionsCount(true)

            while (!connectedClientSocketOnServer.isClosed) {
                val reader =
                    DataInputStream(BufferedInputStream(connectedClientSocketOnServer.getInputStream()))

//                        try {
                val messageType = AppMessageType.fromChar(reader.readChar())

                when (messageType) {
                    AppMessageType.INITIAL -> {
                        setupUserData(reader)
                    }

                    AppMessageType.VOICE -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            receiveVoiceMessage(reader = reader)
                        }
                    }

                    AppMessageType.CONTACT -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            receiveContactMessage(reader = reader)
                        }
                    }

                    AppMessageType.TEXT -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            receiveTextMessage(reader = reader)
                        }
                    }

                    AppMessageType.FILE -> {
                        receiveFile(reader = reader)
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

    private fun connectToServer(serverIpAddress: String, serverPort: Int) {
        log("connecting to server - $serverIpAddress:$serverPort")
        try {
            //create client socket
            clientSocket = Socket(serverIpAddress, serverPort)
            clientWriter = DataOutputStream(clientSocket.getOutputStream())
            log("client writer initialized - $clientWriter")

            lifecycleScope.launch(Dispatchers.IO) {
                initializeUser(clientWriter)
                log("user initialized ")
            }

            viewModel.updateClientConnectionStatus(ClientConnectionStatus.Connected)
            viewModel.updateConnectionsCount(true)

            //received outcome messages here
            while (!clientSocket.isClosed) {
                val reader = DataInputStream(BufferedInputStream(clientSocket.getInputStream()))

//                try {
                val dataType = AppMessageType.fromChar(reader.readChar())
                log("incoming message type - $dataType")

                when (dataType) {
                    AppMessageType.INITIAL -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            setupUserData(reader = reader)
                        }
                    }

                    AppMessageType.VOICE -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            receiveVoiceMessage(reader = reader)
                        }
                    }

                    AppMessageType.TEXT -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            receiveTextMessage(reader = reader)
                        }
                    }

                    AppMessageType.CONTACT -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            receiveContactMessage(reader = reader)
                        }
                    }

                    AppMessageType.FILE -> {
                        receiveFile(reader = reader)
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
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.UnknownHostException))
        } catch (exception: IOException) {
            exception.printStackTrace()
            //could not connect to a server
            log("connectToServer: IOException ".uppercase())
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.IOException))
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

    private fun initializeUser(writer: DataOutputStream) {
        val userUniqueId = runBlocking(Dispatchers.IO) { viewModel.getUniqueDeviceId() }
        val type = AppMessageType.INITIAL.identifier.code
        try {
            writer.writeChar(type)
            writer.writeUTF(userUniqueId)
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

    private fun setupUserData(reader: DataInputStream) {
        val uniqueDeviceId = reader.readUTF()
        log("unique device id - $uniqueDeviceId")
        viewModel.loadChatHistory(uniqueDeviceId)
    }

    /**Socket Sending Functions*/

    private fun sendClientMessage(message: ChatMessageEntity) {
        if (!clientSocket.isClosed) {
            when (message.type) {
                AppMessageType.TEXT -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendTextMessage(writer = clientWriter, textMessage = message)
                    }
                }

                AppMessageType.VOICE -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendVoiceMessage(writer = clientWriter, voiceMessage = message)
                    }
                }

                AppMessageType.CONTACT -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendContactMessage(writer = clientWriter, contactMessage = message)
                    }
                }

                AppMessageType.FILE -> {
                    lifecycleScope.launch {
                        sendFileMessages(writer = clientWriter, messages = listOf(message))
                    }
                }

                else -> {
                    /** Just ignore */
                }
            }
        } else {
            log("send client message: client socket is closed ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
        }
    }

    private fun sendHostMessage(message: ChatMessageEntity) {
        if (!connectedClientSocketOnServer.isClosed) {
            when (message.type) {
                AppMessageType.TEXT -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendTextMessage(writer = connectedClientWriter, textMessage = message)
                    }
                }

                AppMessageType.VOICE -> {
                    lifecycleScope.launch {
                        sendVoiceMessage(writer = connectedClientWriter, voiceMessage = message)
                    }
                }

                AppMessageType.CONTACT -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendContactMessage(writer = connectedClientWriter, contactMessage = message)
                    }
                }

                AppMessageType.FILE -> {
                    lifecycleScope.launch {
                        sendFileMessages(writer = connectedClientWriter, messages = listOf(message))
                    }
                }

                else -> {
                    /** Just ignore */
                }
            }
        } else {
            log("send host message: client socket is closed ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
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

    //5-fragment lifecycle callback
    override fun onStart() {
        super.onStart()
        viewModel.checkReadContactsPermission()
    }

    //6-fragment lifecycle callback
    override fun onResume() {
        super.onResume()
        //todo - here handle connected devices
        val peerListListener = WifiP2pManager.PeerListListener { peerList ->
            val peers = viewModel.state.value.availableWifiNetworks
            val refreshedPeers = peerList.deviceList
            if (refreshedPeers != peers) {
                viewModel.handleAvailableWifiListChange(refreshedPeers.toList())
            }
            if (peers.isEmpty()) {
                log("No devices found")
                return@PeerListListener
            }
        }
        receiver = WiFiDirectBroadcastReceiver(
            wifiP2pManager = wifiP2PManager,
            channel = channel,
            peerListListener = peerListListener,
            networkEventHandler = { networkEvent ->
                viewModel.handleNetworkEvents(networkEvent)
            }
        )
        requireActivity().registerReceiver(receiver, intentFilter)
    }

    //7-fragment lifecycle callback
    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(receiver)
    }

    //8-fragment lifecycle callback
    override fun onDestroyView() {
        super.onDestroyView()
        if (::connectedClientSocketOnServer.isInitialized) {
            connectedClientSocketOnServer.close()
        }
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
        if (::clientSocket.isInitialized) {
            clientSocket.close()
        }
    }

}