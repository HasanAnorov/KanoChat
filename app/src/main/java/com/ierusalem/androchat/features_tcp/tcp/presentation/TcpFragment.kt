package com.ierusalem.androchat.features_tcp.tcp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppMessageType
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.core.constants.Constants.FILE_PROVIDER_AUTHORITY
import com.ierusalem.androchat.core.constants.Constants.SOCKET_DEFAULT_BUFFER_SIZE
import com.ierusalem.androchat.core.constants.Constants.generateUniqueFileName
import com.ierusalem.androchat.core.constants.Constants.getCurrentTime
import com.ierusalem.androchat.core.ui.components.PermissionDialog
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.core.utils.getAudioFileDuration
import com.ierusalem.androchat.core.utils.getExtensionFromFilename
import com.ierusalem.androchat.core.utils.getFileExtensionFromUri
import com.ierusalem.androchat.core.utils.getFileNameFromUri
import com.ierusalem.androchat.core.utils.getFileNameWithoutExtension
import com.ierusalem.androchat.core.utils.getFileSizeInReadableFormat
import com.ierusalem.androchat.core.utils.getMimeType
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.openAppSettings
import com.ierusalem.androchat.core.utils.readableFileSize
import com.ierusalem.androchat.core.utils.shortToast
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.FileMessageState
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features_tcp.server.ServerDefaults
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_tcp.server.wifidirect.Reason
import com.ierusalem.androchat.features_tcp.server.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ClientConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ContactsMessageItem
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
import com.ierusalem.androchat.features_tcp.tcp_chat.presentation.components.ContactListContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            }
        }
    }

    //todo - here handle connected devices
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
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

    //1-fragment lifecycle callback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gson = Gson()
    }

    private val readContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.handleEvents(TcpScreenEvents.ReadContactPermissionChanged(isGranted))
        }

    private val getFilesLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_CANCELED -> {
                log("onActivityResult: RESULT CANCELED ")
            }

            Activity.RESULT_OK -> {
                val contentResolver = activity?.contentResolver!!
                val intent: Intent = result.data!!
                val uri = intent.data!!

                val resourceDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}")
                if (!resourceDirectory.exists()) {
                    resourceDirectory.mkdir()
                }

                val fileName = uri.getFileNameFromUri(contentResolver)
                var file = File(resourceDirectory, fileName)
                if (file.exists()) {
                    log("same file found in folder, generating unique name ...")
                    val fileNameWithoutExt = fileName.getFileNameWithoutExtension()
                    log("file name without ext - $fileNameWithoutExt")
                    val uniqueFileName =
                        generateUniqueFileName(resourceDirectory.toString(), fileNameWithoutExt, file.extension)
                    log("unique file name - $uniqueFileName")
                    file = File(uniqueFileName)
                }

                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val fileOutputStream = FileOutputStream(file)
                inputStream?.copyTo(fileOutputStream)
                fileOutputStream.close()

                log("file info, size - ${uri.getFileSizeInReadableFormat(contentResolver)} - file - $file")


                when (viewModel.state.value.generalConnectionStatus) {
                    GeneralConnectionStatus.Idle -> {
                        /** do nothing here */
                    }

                    GeneralConnectionStatus.ConnectedAsClient -> {
                        val fileMessage = ChatMessage.FileMessage(
                            formattedTime = getCurrentTime(),
                            filePath = file.path,
                            fileName = file.name,
                            fileSize = file.length().readableFileSize(),
                            fileExtension = file.extension,
                            fileState = FileMessageState.Loading(0),
                            isFromYou = true
                        )
                        sendClientMessage(fileMessage)
                    }

                    GeneralConnectionStatus.ConnectedAsHost -> {
                        val fileMessage = ChatMessage.FileMessage(
                            formattedTime = getCurrentTime(),
                            filePath = file.path,
                            fileName = file.name,
                            fileSize = file.length().readableFileSize(),
                            fileExtension = file.extension,
                            fileState = FileMessageState.Loading(0),
                            isFromYou = true
                        )
                        sendHostMessage(fileMessage)
                    }
                }
            }
        }
    }


    private fun showFileChooser() {
        val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
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
                                                            val contactMessage =
                                                                ChatMessage.ContactMessage(
                                                                    formattedTime = getCurrentTime(),
                                                                    isFromYou = true,
                                                                    contactName = contact.contactName,
                                                                    contactNumber = contact.phoneNumber
                                                                )
                                                            sendClientMessage(contactMessage)
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
                                                            val contactMessage =
                                                                ChatMessage.ContactMessage(
                                                                    formattedTime = getCurrentTime(),
                                                                    isFromYou = true,
                                                                    contactName = contact.contactName,
                                                                    contactNumber = contact.phoneNumber
                                                                )
                                                            sendHostMessage(contactMessage)
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
                    if (uiState.shouldShowPermissionDialog) {
                        PermissionDialog(
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                Manifest.permission.READ_CONTACTS
                            ),
                            onDismiss = { viewModel.updateShowPermissionRequestState(false) },
                            onOkClick = {
                                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                viewModel.updateShowPermissionRequestState(false)
                            },
                            onGoToAppSettingsClick = {
                                openAppSettings()
                                viewModel.updateShowPermissionRequestState(false)
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
    @RequiresApi(Build.VERSION_CODES.Q)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    @Suppress("MissingPermission")
    private fun createGroup() {
        viewModel.updateHotspotDiscoveryStatus(HotspotNetworkingStatus.LaunchingHotspot)
        val config = getConfiguration()
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
            wifiP2PManager.createGroup(
                channel, getConfiguration(), listener
            )
        } else {
            log("Creating group without custom configuration")
            wifiP2PManager.createGroup(
                channel, listener
            )
        }
    }

    private fun getConfiguration(): WifiP2pConfig? {
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
        //val band = getPreferredBand()
        val band = WifiP2pConfig.GROUP_OWNER_BAND_2GHZ
        return WifiP2pConfig.Builder().setNetworkName(ssid).setPassphrase(passwd)
            .setGroupOperatingBand(band).build()
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
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Failed to stop network: ${r.displayReason}")
            }
        }
        wifiP2PManager.removeGroup(channel, listener)
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


    @SuppressLint("MissingPermission")
    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

            TcpScreenNavigation.OnReadContactsRequest -> {
                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }

            is TcpScreenNavigation.HandlePickingMultipleMedia -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (viewModel.state.value.generalConnectionStatus) {
                        GeneralConnectionStatus.Idle -> {}
                        GeneralConnectionStatus.ConnectedAsHost -> {
                            val fileMessages = mutableListOf<ChatMessage.FileMessage>()
                            navigation.medias.forEach { imageUri ->
                                //todo - maybe username should be removed here
                                val fileMessage = ChatMessage.FileMessage(
                                    formattedTime = getCurrentTime(),
                                    isFromYou = true,
                                    filePath = imageUri.toString(),
                                    fileName = imageUri.getFileNameFromUri(requireContext().contentResolver),
                                    fileSize = imageUri.getFileSizeInReadableFormat(
                                        requireContext().contentResolver
                                    ),
                                    fileExtension = imageUri.getFileExtensionFromUri(
                                        requireContext().contentResolver
                                    )
                                )
                                fileMessages.add(fileMessage)
                            }
                            sendFileMessages(
                                writer = connectedClientWriter,
                                messages = fileMessages
                            )
                        }

                        GeneralConnectionStatus.ConnectedAsClient -> {
                            val fileMessages = mutableListOf<ChatMessage.FileMessage>()
                            navigation.medias.forEach { imageUri ->
                                //todo - maybe username should be removed here
                                val fileMessage = ChatMessage.FileMessage(
                                    formattedTime = getCurrentTime(),
                                    isFromYou = true,
                                    filePath = imageUri.toString(),
                                    fileName = imageUri.getFileNameFromUri(requireContext().contentResolver),
                                    fileSize = imageUri.getFileSizeInReadableFormat(
                                        requireContext().contentResolver
                                    ),
                                    fileExtension = imageUri.getFileExtensionFromUri(
                                        requireContext().contentResolver
                                    )
                                )
                                fileMessages.add(fileMessage)
                            }
                            sendFileMessages(writer = clientWriter, messages = fileMessages)
                        }
                    }
                }
            }

            is TcpScreenNavigation.OnContactItemClick -> {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.setData(Uri.parse("tel:${navigation.message.contactNumber}"))
                startActivity(intent)
            }

            is TcpScreenNavigation.OnFileItemClick -> {
                try {
                    val file: File
                    if (navigation.message.isFromYou) {
                        //todo optimize this later
                        val uri = Uri.parse(navigation.message.filePath)
                        val inputStream =
                            requireContext().contentResolver.openInputStream(uri)
                        val filePathToSave = context?.cacheDir
                        file = File(filePathToSave, navigation.message.fileName)
                        val fileOutputStream = FileOutputStream(file)
                        inputStream?.copyTo(fileOutputStream)
                        fileOutputStream.close()
                    } else {
                        file = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}"),
                            navigation.message.fileName
                        )
                    }
                    val uri: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        FILE_PROVIDER_AUTHORITY,
                        file
                    )
                    //todo optimize this later
                    val uriX = Uri.parse(navigation.message.filePath)
                    val mimeType = uriX.getMimeType(requireContext())
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.open_with))
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // no Activity to handle this kind of files
                    shortToast(getString(R.string.no_application_found_to_open_this_file))
                    log("can not open a file !")
                    e.printStackTrace()
                }
            }

            TcpScreenNavigation.OnNavIconClick -> {
                findNavController().popBackStack()
            }

            TcpScreenNavigation.OnStopHotspotNetworking -> {
                stopHotspotNetworking()
            }

            TcpScreenNavigation.OnStopP2PDiscovery -> {
                stopPeerDiscovery()
            }

            TcpScreenNavigation.WifiDisabledCase -> {
                handleWifiDisabledCase()
            }

            TcpScreenNavigation.OnSettingsClick -> {
                findNavController().navigate(R.id.action_tcpFragment_to_settingsFragment)
            }

            TcpScreenNavigation.ShowFileChooserClick -> {
                showFileChooser()
            }

            is TcpScreenNavigation.OnCreateServerClick -> {
                log("create Server - " + viewModel.state.value.groupOwnerAddress)
                CoroutineScope(Dispatchers.Default).launch {
                    createServer(serverPort = navigation.portNumber)
                }
            }

            //fixme clarify error
            TcpScreenNavigation.OnStartHotspotNetworking -> {
                lifecycleScope.launch {
                    if (permissionGuard.canCreateNetwork()) {
                        if (ServerDefaults.canUseCustomConfig()) {
                            createGroup()
                        } else {
                            viewModel.updateHasErrorOccurredDialog(TcpScreenDialogErrors.AndroidVersion10RequiredForGroupNetworking)
                        }
                    } else {
                        log("Permissions not granted!")
                        locationPermissionRequest.launch(
                            permissionGuard.requiredPermissions.toTypedArray()
                        )
                    }
                }
            }

            is TcpScreenNavigation.OnConnectToServerClick -> {
                CoroutineScope(Dispatchers.IO).launch {
                    connectToServer(
                        serverIpAddress = navigation.serverIpAddress,
                        serverPort = navigation.portNumber
                    )
                }
            }

            is TcpScreenNavigation.OnConnectToWifiClick -> {
                connectToWifi(navigation.wifiP2pDevice)
            }

            is TcpScreenNavigation.OnErrorsOccurred -> {
                Toast.makeText(
                    requireContext(), navigation.tcpScreenErrors.errorMessage, Toast.LENGTH_SHORT
                ).show()
            }

            is TcpScreenNavigation.SendHostMessage -> {
                CoroutineScope(Dispatchers.IO).launch {
                    sendHostMessage(navigation.message)
                }
            }

            is TcpScreenNavigation.SendClientMessage -> {
                CoroutineScope(Dispatchers.IO).launch {
                    sendClientMessage(navigation.message)
                }
            }

            TcpScreenNavigation.OnDiscoverP2PClick -> {
                lifecycleScope.launch {
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
                            permissionGuard.requiredPermissions.toTypedArray()
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToWifi(wifiP2pDevice: WifiP2pDevice) {
        log("connectToWifi: $wifiP2pDevice ")
        val config = WifiP2pConfig().apply {
            deviceAddress = wifiP2pDevice.deviceAddress
            wps.setup = WpsInfo.PBC
        }
        wifiP2PManager.connect(
            channel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    // WiFiDirectBroadcastReceiver notifies us. Ignore for now
                    log("success: connected to wifi - ${wifiP2pDevice.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    log("failure: failure on wifi connection ")
                    viewModel.emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.FailedToConnectToWifiDevice))
                }
            }
        )
    }

//    private fun receiveFile(reader: DataInputStream) {
//        log("receiving file ...")
//        val downloadsDirectory =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}")
//        if (!downloadsDirectory.exists()) {
//            downloadsDirectory.mkdirs()
//            log("Created directory: ${downloadsDirectory.absolutePath}")
//        }
//
//        val fileCount = reader.readInt()
//        log("file count - $fileCount")
//        for (i in 0 until fileCount) {
//            log("file order - $i")
//
//            //reading file name
//            val filename = reader.readUTF()
//            log("Expected file name - $filename")
//
//            // Check if a file with the same name already exists, generate unique name if necessary
//            var file = File(downloadsDirectory, filename)
//            if (file.exists()) {
//                log("same file found in folder, generating unique name ...")
//                val fileName = filename.getFileNameWithoutExtension()
//                val uniqueFileName =
//                    generateUniqueFileName(downloadsDirectory.toString(), fileName, file.extension)
//                log("unique file name - $uniqueFileName")
//                file = File(uniqueFileName)
//            }
//
//            var bytes = 0
//            var bytesForPercentage = 0L
//            // Create FileOutputStream to write the received file
//            val fileOutputStream = FileOutputStream(file)
//
//            // Read the expected file size
//            var fileSize: Long = reader.readLong() // read file size
//            val fileSizeForPercentage = fileSize
//
//            val message = ChatMessage.FileMessage(
//                formattedTime = getCurrentTime(),
//                filePath = file.path,
//                fileName = file.name,
//                fileSize =fileSize.readableFileSize(),
//                fileExtension = file.extension,
//                fileState = FileMessageState.Loading(0),
//                isFromYou = false
//            )
//            viewModel.insertMessage(message)
//
//            val buffer = ByteArray(SOCKET_DEFAULT_BUFFER_SIZE)
//            while (fileSize > 0
//                && (reader.read(
//                    buffer, 0,
//                    min(buffer.size.toDouble(), fileSize.toDouble()).toInt()
//                ).also { bytes = it })
//                != -1
//            ) {
//                // Here we write the file using write method
//                fileOutputStream.write(buffer, 0, bytes)
//                fileSize -= bytes.toLong()
//
//                bytesForPercentage += bytes.toLong()
//                val percentage =
//                    (bytesForPercentage.toDouble() / fileSizeForPercentage.toDouble() * 100).toInt()
//                val tempPercentage =
//                    ((bytesForPercentage - bytes.toLong()) / fileSizeForPercentage.toDouble() * 100).toInt()
//                if (percentage != tempPercentage) {
//                    log("progress - $percentage")
//                    val newState = FileMessageState.Loading(percentage)
//                    viewModel.updatePercentageOfReceivingFile(message, newState)
//                }
//            }
//            fileOutputStream.close()
//            val newState = FileMessageState.Success
//            viewModel.updatePercentageOfReceivingFile(message, newState)
//            log("file received successfully")
//        }
//    }

    private fun receiveFile(reader: DataInputStream) {
        log("receiving file ...")
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}")
        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs()
            log("Created directory: ${downloadsDirectory.absolutePath}")
        }

        val fileCount = reader.readInt()
        log("file count - $fileCount")
        for (i in 0 until fileCount) {
            log("file order - $i")

            //reading file name
            val filename = reader.readUTF()
            log("Expected file name - $filename")


            var bytes = 0
            var bytesForPercentage = 0L

            // Read the expected file size
            var fileSize: Long = reader.readLong() // read file size
            val fileSizeForPercentage = fileSize
            log("file size - $fileSize")

            // Check if a file with the same name already exists, generate unique name if necessary
            var file = File(downloadsDirectory, filename)
            if (file.exists()) {
                log("same file found in folder, generating unique name ...")
                val fileName = filename.getFileNameWithoutExtension()
                val fileExtension = filename.getExtensionFromFilename()
                val uniqueFileName =
                    generateUniqueFileName(downloadsDirectory.toString(), fileName, fileExtension)
                log("unique file name - $uniqueFileName")
                file = File(uniqueFileName)
            }

            // Create FileOutputStream to write the received file
            val fileOutputStream = FileOutputStream(file)

            val message = ChatMessage.FileMessage(
                formattedTime = getCurrentTime(),
                filePath = file.path,
                fileName = file.name,
                fileSize = fileSize.readableFileSize(),
                fileExtension = filename.getExtensionFromFilename(),
                fileState = FileMessageState.Loading(0),
                isFromYou = false
            )
            viewModel.insertMessage(message)

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
//                    viewModel.updatePercentageOfReceivingFile(message, newState)
//                    val newState = FileMessageState.Loading(percentage)
                }
            }
            fileOutputStream.close()
            val newState = FileMessageState.Success
            viewModel.updatePercentageOfReceivingFile(message, newState)
            log("file received successfully")
        }
//        reader.close()
    }


    private fun receiveVoiceMessage(reader: DataInputStream) {
        log("receiving file ...")

        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}")
        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs()
            log("Created directory: ${downloadsDirectory.absolutePath}")
        }

        //reading file name
        val filename = reader.readUTF()
        var fileNameForUi = filename
        log("Expected file name - $filename")

        // Check if a file with the same name already exists, generate unique name if necessary
        var file = File(downloadsDirectory, filename)
        if (file.exists()) {
            log("same file found in folder, generating unique name ...")
            val fileName = filename.getFileNameWithoutExtension()
            val fileExtension = filename.getExtensionFromFilename()
            val uniqueFileName =
                generateUniqueFileName(downloadsDirectory.toString(), fileName, fileExtension)
            fileNameForUi = Uri.parse(uniqueFileName).lastPathSegment
            log("unique file name - $uniqueFileName")
            file = File(uniqueFileName)
        }

        var bytes = 0
        var bytesForPercentage = 0L
        // Create FileOutputStream to write the received file
        val fileOutputStream = FileOutputStream(file)

        // Read the expected file size
        var fileSize: Long = reader.readLong() // read file size
        val fileSizeForPercentage = fileSize

        val message = ChatMessage.VoiceMessage(
            formattedTime = getCurrentTime(),
            filePath = Uri.fromFile(file).toString(),
            fileName = fileNameForUi,
            fileSize = Formatter.formatShortFileSize(
                requireContext(),
                fileSize
            ),
            fileExtension = filename.getExtensionFromFilename(),
            duration = file.getAudioFileDuration(),
            isFromYou = false,
            fileState = FileMessageState.Loading(0)
        )
        viewModel.insertMessage(message)

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
                viewModel.updatePercentageOfReceivingFile(message, newState)
            }
        }
        fileOutputStream.close()
        val newState = FileMessageState.Success
        viewModel.updatePercentageOfReceivingFile(message, newState)
        log("file received successfully")
    }

    private suspend fun sendVoiceMessage(
        writer: DataOutputStream,
        voiceMessage: ChatMessage.VoiceMessage
    ) {
        log("sending voice message ...")
        viewModel.insertMessage(voiceMessage)

        try {
            withContext(Dispatchers.IO) {
                //sending file type
                val type = AppMessageType.VOICE.identifier.code
                writer.writeChar(type)

                //sending file name
                writer.writeUTF(voiceMessage.fileName)

                //todo optimize this later
                val uri = Uri.parse(voiceMessage.filePath)
                val inputStream =
                    requireContext().contentResolver.openInputStream(uri)
                val filePathToSave = context?.cacheDir
                val file = File(filePathToSave, voiceMessage.fileName)
                val fileOutputStream = FileOutputStream(file)
                inputStream?.copyTo(fileOutputStream)
                fileOutputStream.close()

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
                            viewModel.updatePercentageOfReceivingFile(voiceMessage, newState)
                        }
                    }
                }
                // close the file here
                fileInputStream.close()
                withContext(Dispatchers.Main) {
                    log("file sent successfully")
                    val newState = FileMessageState.Success
                    viewModel.updatePercentageOfReceivingFile(voiceMessage, newState)
                }
            }
        } catch (exception: IOException) {
            withContext(Dispatchers.Main) {
                log("file sent failed")
                val newState = FileMessageState.Failure
                viewModel.updatePercentageOfReceivingFile(voiceMessage, newState)
            }
        } catch (error: Exception) {
            withContext(Dispatchers.Main) {
                log("file sent failed")
                val newState = FileMessageState.Failure
                viewModel.updatePercentageOfReceivingFile(voiceMessage, newState)
            }
        }
    }

    private suspend fun sendFileMessages(
        writer: DataOutputStream,
        messages: List<ChatMessage.FileMessage>
    ) {
        log("sending file , writer is - $writer ...")
        withContext(Dispatchers.IO) {

            //sending file type
            val type = AppMessageType.FILE.identifier.code
            writer.writeChar(type)

            //sending file count
            writer.writeInt(messages.size)
            log("messages size - ${messages.size}")

            messages.forEach { fileMessage ->
//                try {
                val resourceDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}")
                val file = File(resourceDirectory, fileMessage.fileName)
                log("sending file info: file size - ${file.length()} - ${file.name}")


                viewModel.insertMessage(fileMessage)

                //sending file name
                writer.writeUTF(fileMessage.fileName)

//                val uri = Uri.parse(fileMessage.filePath)
//                val inputStream =
//                    requireContext().contentResolver.openInputStream(uri)
//                val filePathToSave = context?.cacheDir
//                val file = File(filePathToSave, fileMessage.fileName)
//                val fileOutputStream = FileOutputStream(file)
//                inputStream?.copyTo(fileOutputStream)
//                fileOutputStream.close()

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
                            viewModel.updatePercentageOfReceivingFile(fileMessage, newState)
                        }
                    }
                }
                // close the file here
                fileInputStream.close()
                withContext(Dispatchers.Main) {
                    log("file sent successfully")
                    val newState = FileMessageState.Success
                    viewModel.updatePercentageOfReceivingFile(fileMessage, newState)
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

    private suspend fun createServer(serverPort: Int) {
        log("creating server ...")
        viewModel.updateHostConnectionStatus(HostConnectionStatus.Creating)

        withContext(Dispatchers.IO) {
            try {
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
                    initializeUser(connectedClientWriter)
                    log("New client : $connectedClientSocketOnServer ")
                    viewModel.updateConnectionsCount(true)

                    while (!connectedClientSocketOnServer.isClosed) {
                        val reader =
                            DataInputStream(BufferedInputStream(connectedClientSocketOnServer.getInputStream()))

//                        try {
                            val dataType = AppMessageType.fromChar(reader.readChar())

                            when (dataType) {
                                AppMessageType.INITIAL -> {
                                    setupUserData(reader)
                                }

                                AppMessageType.VOICE -> {
                                    receiveVoiceMessage(reader)
                                }

                                AppMessageType.CONTACT -> {
                                    val receivedMessage = reader.readUTF()
                                    log("host incoming contact message - $receivedMessage")
                                    val contactMessageItem =
                                        gson.fromJson(
                                            receivedMessage,
                                            ContactsMessageItem::class.java
                                        )
                                    val contactMessage = ChatMessage.ContactMessage(
                                        formattedTime = getCurrentTime(),
                                        contactName = contactMessageItem.contactName,
                                        contactNumber = contactMessageItem.contactNumber,
                                        isFromYou = false
                                    )
                                    viewModel.insertMessage(contactMessage)
                                }

                                AppMessageType.TEXT -> {
                                    val receivedMessage = reader.readUTF()
                                    log("host incoming message - $receivedMessage")

                                    val message = ChatMessage.TextMessage(
                                        formattedTime = getCurrentTime(),
                                        message = receivedMessage.toString(),
                                        isFromYou = false
                                    )
                                    viewModel.insertMessage(message)
                                }

                                AppMessageType.FILE -> {
                                    receiveFile(reader = reader)
                                }

                                AppMessageType.UNKNOWN -> {
                                    //currently not handled
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
            } catch (e: IOException) {
                e.printStackTrace()
                serverSocket.close()
                //change server title status
                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)

            } catch (e: SecurityException) {
                e.printStackTrace()
                serverSocket.close()
                //change server title status
                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)

                //if a security manager exists and its checkConnect method doesn't allow the operation.
                log("createServer: SecurityException ")
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                serverSocket.close()
                //change server title status
                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)
                //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
                log("createServer: IllegalArgumentException ")
            }
        }
    }

    private fun initializeUser(writer: DataOutputStream) {
        val type = AppMessageType.INITIAL.identifier.code
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                writer.writeChar(type)
                writer.writeUTF(viewModel.getUniqueDeviceId())
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
    }

    private fun setupUserData(reader: DataInputStream) {
        val uniqueDeviceId = reader.readUTF()
        log("unique device id - $uniqueDeviceId")
        val isUserExist = viewModel.checkForUserExistence(uniqueDeviceId)
        log("isUserExist - $isUserExist")
        viewModel.loadChatHistory(uniqueDeviceId)
    }

    private fun connectToServer(serverIpAddress: String, serverPort: Int) {
        log("connecting to server - $serverIpAddress:$serverPort")
        try {
            //create client
            clientSocket = Socket(serverIpAddress, serverPort)
            clientWriter = DataOutputStream(clientSocket.getOutputStream())
            initializeUser(clientWriter)
            log("client writer initialized - $clientWriter")

            viewModel.updateClientConnectionStatus(ClientConnectionStatus.Connected)
            viewModel.updateConnectionsCount(true)
            log("client socket - $clientSocket")

            //received outcome messages here
            while (!clientSocket.isClosed) {
                val reader = DataInputStream(BufferedInputStream(clientSocket.getInputStream()))

//                try {
                    val dataType = AppMessageType.fromChar(reader.readChar())
                    log("incoming message type - $dataType")

                    when (dataType) {
                        AppMessageType.INITIAL -> {
                            setupUserData(reader)
                        }

                        AppMessageType.VOICE -> {

                        }

                        AppMessageType.TEXT -> {
                            val receivedMessage = reader.readUTF()
                            log("host incoming message - $receivedMessage")

                            val message = ChatMessage.TextMessage(
                                formattedTime = getCurrentTime(),
                                message = receivedMessage.toString(),
                                isFromYou = false
                            )
                            viewModel.insertMessage(message)
                        }

                        AppMessageType.CONTACT -> {
                            val receivedMessage = reader.readUTF()
                            log("client incoming contact message - $receivedMessage")
                            val contactMessageItem =
                                gson.fromJson(receivedMessage, ContactsMessageItem::class.java)
                            val contactMessage = ChatMessage.ContactMessage(
                                formattedTime = getCurrentTime(),
                                contactName = contactMessageItem.contactName,
                                contactNumber = contactMessageItem.contactNumber,
                                isFromYou = false
                            )
                            viewModel.insertMessage(contactMessage)
                        }

                        AppMessageType.FILE -> {
                            receiveFile(reader = reader)
                        }

                        AppMessageType.UNKNOWN -> {}
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

    private fun sendContactMessage(
        writer: DataOutputStream,
        contactMessage: ChatMessage.ContactMessage
    ) {
        val contactsMessageItem = ContactsMessageItem(
            contactName = contactMessage.contactName,
            contactNumber = contactMessage.contactNumber
        )
        val contactsStringForm = gson.toJson(contactsMessageItem)

        try {
            writer.writeChar(contactMessage.messageType.identifier.code)
            writer.writeUTF(contactsStringForm)
            viewModel.insertMessage(contactMessage)
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

    private fun sendTextMessage(writer: DataOutputStream, textMessage: ChatMessage.TextMessage) {
        log("sending text message from client - $textMessage")

        val data = textMessage.message

        try {
            writer.writeChar(textMessage.messageType.identifier.code)
            writer.writeUTF(data)
            viewModel.insertMessage(textMessage)
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

    private fun sendClientMessage(message: ChatMessage) {
        if (!clientSocket.isClosed) {
            when (message) {
                is ChatMessage.TextMessage -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendTextMessage(writer = clientWriter, textMessage = message)
                    }
                }

                is ChatMessage.VoiceMessage -> {
                    lifecycleScope.launch {
                        sendVoiceMessage(writer = clientWriter, voiceMessage = message)
                    }
                }

                is ChatMessage.ContactMessage -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendContactMessage(writer = clientWriter, contactMessage = message)
                    }
                }

                is ChatMessage.FileMessage -> {
                    lifecycleScope.launch {
                        sendFileMessages(
                            writer = clientWriter,
                            messages = listOf(message),
                        )
                    }
                }
            }
        } else {
            log("send client message: client socket is closed ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
        }
    }

    private fun sendHostMessage(message: ChatMessage) {
        if (!connectedClientSocketOnServer.isClosed) {
            when (message) {
                is ChatMessage.TextMessage -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendTextMessage(writer = connectedClientWriter, textMessage = message)
                    }
                }

                is ChatMessage.VoiceMessage -> {

                }

                is ChatMessage.ContactMessage -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendContactMessage(writer = connectedClientWriter, contactMessage = message)
                    }
                }

                is ChatMessage.FileMessage -> {
                    lifecycleScope.launch {
                        sendFileMessages(
                            writer = connectedClientWriter,
                            messages = listOf(message),
                        )
                    }
                }
            }
        } else {
            log("send host message: client socket is closed ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
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