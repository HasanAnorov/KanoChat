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
import android.provider.OpenableColumns
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
import com.ierusalem.androchat.core.ui.components.PermissionDialog
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.core.utils.getExtensionFromFilename
import com.ierusalem.androchat.core.utils.getFileNameWithoutExtension
import com.ierusalem.androchat.core.utils.getMimeType
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.core.utils.openAppSettings
import com.ierusalem.androchat.core.utils.toast
import com.ierusalem.androchat.features.auth.register.domain.model.FileState
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.server.ServerDefaults
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_tcp.server.wifidirect.Reason
import com.ierusalem.androchat.features_tcp.server.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_tcp.tcp.domain.state.ClientConnectionStatus
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
import java.util.Calendar
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

    //chatting client side
    private lateinit var clientSocket: Socket

    //audio recording
//    private var audioRecord: AudioRecorder? = null
//    private var audioPlayer: AudioPlayer? = null
//
//    private val file: File by lazy {
//        val f = File("${Environment.DIRECTORY_DOWNLOADS}${File.separator}audio.pcm")
//        if (!f.exists()) {
//            f.createNewFile()
//        }
//        f
//    }
//
//    private val tmpFile: File by lazy {
//        val f = File("${Environment.DIRECTORY_DOWNLOADS}${File.separator}tmp.pcm")
//        if (!f.exists()) {
//            f.createNewFile()
//        }
//        f
//    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Log.d("ahi3646", "Precise location access granted: ")
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Log.d("ahi3646", "Only approximate location access granted: ")
            }

            permissions.getOrDefault(Manifest.permission.NEARBY_WIFI_DEVICES, false) -> {
                // Only approximate location access granted.
                Log.d("ahi3646", "Only approximate location access granted: ")
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
            Log.d("ahi3646", "No devices found")
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
                Log.d("ahi3646", "onActivityResult: RESULT CANCELED ")
            }

            Activity.RESULT_OK -> {
                val contentResolver = activity?.contentResolver
                val data: Intent = result.data!!

                var fileName = "file"
                var fileSize = 0L

                data.data?.let { returnUri ->
                    contentResolver?.query(returnUri, null, null, null, null)
                }?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    fileName = cursor.getString(nameIndex)
                    fileSize = cursor.getLong(sizeIndex)
                }

                log("filename - $fileName, filepath - ${data.data!!} , size - $fileSize")
                when (viewModel.state.value.generalConnectionStatus) {
                    GeneralConnectionStatus.Idle -> {
                        //do nothing
                    }

                    GeneralConnectionStatus.ConnectedAsClient -> {
                        val currentTime = Calendar.getInstance().time
                        val extension = fileName.getExtensionFromFilename()
                        val fileMessage = Message.FileMessage(
                            formattedTime = currentTime.toString(),
                            username = viewModel.state.value.authorMe,
                            filePath = data.data!!,
                            fileName = fileName,
                            fileSize = Formatter.formatShortFileSize(requireContext(), fileSize),
                            fileExtension = extension,
                            fileState = FileState.Loading(0),
                            isFromYou = true
                        )
                        sendClientMessage(fileMessage)
                    }

                    GeneralConnectionStatus.ConnectedAsHost -> {

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
            Log.d("ahi3646", "WifiP2PManager Channel died! Do nothing :D")
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
                    //fixme change tab here
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

                val state by viewModel.state.collectAsStateWithLifecycle()

                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = false
                )

                //todo
//                BackHandler {
//                    if (state.messages.isNotEmpty()) {
//                        //show close dialog here
//                    }
//                }

                AndroChatTheme {
                    if (state.showBottomSheet) {
                        ModalBottomSheet(
                            sheetState = sheetState, onDismissRequest = {
                                viewModel.handleEvents(TcpScreenEvents.UpdateBottomSheetState(false))
                            }, windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            if (state.isReadContactsGranted) {
                                viewModel.handleEvents(TcpScreenEvents.ReadContacts)
                                ContactListContent(contacts = state.contacts,
                                    shareSelectedContacts = {})
                            } else {
                                Box(modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                    content = {
                                        Button(onClick = {
                                            readContactsPermissionLauncher.launch(
                                                Manifest.permission.READ_CONTACTS
                                            )
                                        }) {
                                            Text(text = "Give Permission")
                                        }
                                    })
                            }
                        }
                    }
                    if (state.shouldShowPermissionDialog) {
                        PermissionDialog(isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
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
                            })
                    }

                    TcpScreen(state = state,
                        //try to use pass lambda like this, this will help to avoid extra recomposition
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
            lifecycle = viewLifecycleOwner.lifecycle, action = ::executeNavigation
        )
    }

    // todo - use this method to close servers
    // network clean up should be carried out in viewmodel
    private fun handleWifiDisabledCase() {
        when (viewModel.state.value.generalConnectionStatus) {
            GeneralConnectionStatus.Idle -> {
                //do nothing
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
            log("Creating group")
            wifiP2PManager.createGroup(
                channel, getConfiguration(), listener
            )
        } else {
            log("Creating group1")
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

        wifiP2PManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("Wifi P2P Channel is removed")
                viewModel.updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Idle)
            }

            override fun onFailure(reason: Int) {
                val r = Reason.parseReason(reason)
                log("Failed to stop network: ${r.displayReason}")
            }
        })
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
        wifiP2PManager.stopPeerDiscovery(
            channel, listener
        )
    }

    @SuppressLint("MissingPermission")
    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

            TcpScreenNavigation.OnReadContactsRequest -> {
                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }

            is TcpScreenNavigation.OnFileItemClick -> {
                try {
                    val file: File
                    if(navigation.message.isFromYou){
                        val inputStream = requireContext().contentResolver.openInputStream(navigation.message.filePath)
                        val filePathToSave = context?.cacheDir
                        file = File(filePathToSave, navigation.message.fileName)
                        val fileOutputStream = FileOutputStream(file)
                        inputStream?.copyTo(fileOutputStream)
                        fileOutputStream.close()
                    }else{
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
                    val mimeType = navigation.message.filePath.getMimeType(requireContext())
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooserIntent = Intent.createChooser(intent, getString(R.string.open_with))
                    startActivity(chooserIntent)
                } catch (e: ActivityNotFoundException) {
                    // no Activity to handle this kind of files
                    toast(getString(R.string.no_application_found_to_open_this_file))
                    log("can not open a file !")
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

                        wifiP2PManager.discoverPeers(channel,
                            object : WifiP2pManager.ActionListener {
                                override fun onSuccess() {
                                    Log.d("ahi3646", "onSuccess: discover ")
                                    viewModel.updateP2PDiscoveryStatus(P2PNetworkingStatus.Discovering)
                                }

                                override fun onFailure(reason: Int) {
                                    // Code for when the discovery initiation fails goes here.
                                    // Alert the user that something went wrong.
                                    Log.d("ahi3646", "onFailure: discover $reason ")
                                    viewModel.updateP2PDiscoveryStatus(P2PNetworkingStatus.Failure)
                                }

                            })
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
        Log.d("ahi3646", "connectToWifi: $wifiP2pDevice ")
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
                    Log.d("ahi3646", "success: connected to wifi - ${wifiP2pDevice.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    Log.d("ahi3646", "failure: failure on wifi connection ")
                    viewModel.emitNavigation(TcpScreenNavigation.OnErrorsOccurred(TcpScreenErrors.FailedToConnectToWifiDevice))
                }
            }
        )
    }

    private fun receiveFile(reader: DataInputStream) {
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

        val currentTime = Calendar.getInstance().time
        val message = Message.FileMessage(
            formattedTime = currentTime.toString(),
            username = "from client",
            filePath = Uri.fromFile(file),
            fileName = fileNameForUi,
            fileSize = Formatter.formatShortFileSize(
                requireContext(),
                fileSize
            ),
            fileExtension = filename.getExtensionFromFilename(),
            fileState = FileState.Loading(0),
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
                val newState = FileState.Loading(percentage)
                viewModel.updatePercentageOfReceivingFile(message, newState)
            }
        }
        fileOutputStream.close()
        val newState = FileState.Success
        viewModel.updatePercentageOfReceivingFile(message, newState)
        log("file received successfully")
    }

    private fun sendFile(writer: DataOutputStream, fileMessage: Message.FileMessage) {
        log("sending file ...")
        log("filename - ${fileMessage.fileName}, filepath - ${fileMessage.filePath}")

        viewModel.handleEvents(TcpScreenEvents.InsertMessage(fileMessage))

        val type = AppMessageType.FILE.identifier
        // Here we send the File to Server
        writer.writeChar(type.code)
        //sending file name
        writer.writeUTF(fileMessage.fileName)
        log("sending file name - ${fileMessage.fileName}")

        val inputStream = requireContext().contentResolver.openInputStream(fileMessage.filePath)
        val filePathToSave = context?.cacheDir
        val file = File(filePathToSave, fileMessage.fileName)
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
                log("progress - $percentage")
                val newState = FileState.Loading(percentage)
                viewModel.updatePercentageOfReceivingFile(fileMessage, newState)
            }
        }
        // close the file here
        fileInputStream.close()
        val newState = FileState.Success
        viewModel.updatePercentageOfReceivingFile(fileMessage, newState)
        log("file sent successfully")
    }

    private suspend fun createServer(serverPort: Int) {
        log("creating server".uppercase())
        viewModel.updateHostConnectionStatus(HostConnectionStatus.Creating)

        withContext(Dispatchers.IO) {
            serverSocket = ServerSocket(serverPort)
            if (serverSocket.isBound) {
                viewModel.updateHostConnectionStatus(HostConnectionStatus.Created)
                viewModel.updateConnectionsCount(true)
            }
            while (!serverSocket.isClosed) {
                connectedClientSocketOnServer = serverSocket.accept()
                Log.d("ahi3646", "New client : $connectedClientSocketOnServer ")
                viewModel.updateConnectionsCount(true)

                while (!connectedClientSocketOnServer.isClosed) {
                    val reader =
                        DataInputStream(BufferedInputStream(connectedClientSocketOnServer.getInputStream()))
                    val dataType = AppMessageType.fromChar(reader.readChar())

                    when (dataType) {
                        AppMessageType.INITIAL -> {

                        }

                        AppMessageType.TEXT -> {
                            val receivedMessage = reader.readUTF()
                            log("host incoming message - $receivedMessage")

                            val currentTime = Calendar.getInstance().time
                            val message = Message.TextMessage(
                                username = "from client",
                                formattedTime = currentTime.toString(),
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

//                        try {
//                            val dataType = AppMessageType.fromChar(reader.readChar())
//                            log("incoming message type - $dataType")
//
//                            when (dataType) {
//                                AppMessageType.INITIAL -> {
//
//                                }
//
//                                AppMessageType.TEXT -> {
//                                    val length = reader.readInt()
//                                    val messageByte = ByteArray(length)
//                                    var end = false
//                                    val dataString = java.lang.StringBuilder(length)
//                                    var totalBytesRead = 0
//                                    while (!end) {
//                                        val currentBytesRead: Int = reader.read(messageByte)
//                                        totalBytesRead += currentBytesRead
//                                        if (totalBytesRead <= length) {
//                                            dataString.append(
//                                                    String(
//                                                        messageByte,
//                                                        0,
//                                                        currentBytesRead,
//                                                        StandardCharsets.UTF_8
//                                                    )
//                                                )
//                                        } else {
//                                            dataString.append(
//                                                    String(
//                                                        messageByte,
//                                                        0,
//                                                        length - totalBytesRead + currentBytesRead,
//                                                        StandardCharsets.UTF_8
//                                                    )
//                                                )
//                                        }
//                                        if (dataString.length >= length) {
//                                            end = true
//                                            log("host incoming message - $dataString")
//                                        }
//                                    }
//                                    //                            val inputData = reader.readUTF()
////                            val message = gson.fromJson(
////                                inputData,
////                                Message::class.java
////                            )
////                            viewModel.insertMessage(message)
//                                }
//
//                                AppMessageType.FILE -> {
//                                    receiveFile(dataInputStream = reader, fileName = "4228.pdf")
//                                }
//
//                                AppMessageType.UNKNOWN -> {}
//                            }
//                        } catch (e: EOFException) {
//                            //if the IP address of the host could not be determined.
//                            Log.d("ahi3646", "createServer: EOFException")
//                            connectedClientSocketOnServer.close()
//                            viewModel.updateConnectionsCount(false)
//                            log("in while - ${connectedClientSocketOnServer.isClosed} - $connectedClientSocketOnServer")
//                            viewModel.handleEvents(
//                                TcpScreenEvents.OnDialogErrorOccurred(
//                                    TcpScreenDialogErrors.EOException
//                                )
//                            )
//                            //HERE IS THE POINT !!!
//                            try {
//                                reader.close()
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                        } catch (e: IOException) {
//                            //the stream has been closed and the contained
//                            // input stream does not support reading after close,
//                            // or another I/O error occurs
//                            Log.d("ahi3646", "createServer: io exception ")
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
//                            /** here is firing***/
//                            //if the bytes do not represent a valid modified UTF-8 encoding of a string.
//                            Log.d("ahi3646", "createServer: io exception ")
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
//            try {
//                serverSocket = ServerSocket(serverPort)
//                log("server created in : $serverSocket ${serverSocket.localSocketAddress}")
//                if (serverSocket.isBound) {
//                    viewModel.updateHostConnectionStatus(HostConnectionStatus.Created)
//                    viewModel.updateConnectionsCount(true)
//                }
//                while (!serverSocket.isClosed) {
//
//                    connectedClientSocketOnServer = serverSocket.accept()
//                    Log.d("ahi3646", "New client : $connectedClientSocketOnServer ")
//                    viewModel.updateConnectionsCount(true)
//
//                    while (!connectedClientSocketOnServer.isClosed) {
//                        //reading incoming messages ...
//                        val reader =
//                            DataInputStream(BufferedInputStream(connectedClientSocketOnServer.getInputStream()))
//
//                        val dataType = AppMessageType.fromChar(reader.readChar())
//                        log("incoming message type - $dataType")
//
//                        when (dataType) {
//                            AppMessageType.INITIAL -> {
//
//                            }
//
//                            AppMessageType.TEXT -> {
//                                val length = reader.readInt()
//                                val messageByte = ByteArray(length)
//                                var end = false
//                                val dataString = java.lang.StringBuilder(length)
//                                var totalBytesRead = 0
//                                while (!end) {
//                                    val currentBytesRead: Int = reader.read(messageByte)
//                                    totalBytesRead += currentBytesRead
//                                    if (totalBytesRead <= length) {
//                                        dataString.append(
//                                            String(
//                                                messageByte,
//                                                0,
//                                                currentBytesRead,
//                                                StandardCharsets.UTF_8
//                                            )
//                                        )
//                                    } else {
//                                        dataString.append(
//                                            String(
//                                                messageByte,
//                                                0,
//                                                length - totalBytesRead + currentBytesRead,
//                                                StandardCharsets.UTF_8
//                                            )
//                                        )
//                                    }
//                                    if (dataString.length >= length) {
//                                        end = true
//                                        log("host incoming message - $dataString")
//                                    }
//                                }
//                                //                            val inputData = reader.readUTF()
////                            val message = gson.fromJson(
////                                inputData,
////                                Message::class.java
////                            )
////                            viewModel.insertMessage(message)
//                            }
//
//                            AppMessageType.FILE -> {
//                                receiveFile(dataInputStream = reader, fileName = "4228.pdf")
//                            }
//
//                            AppMessageType.UNKNOWN -> {}
//                        }
//
////                        try {
////                            val dataType = AppMessageType.fromChar(reader.readChar())
////                            log("incoming message type - $dataType")
////
////                            when (dataType) {
////                                AppMessageType.INITIAL -> {
////
////                                }
////
////                                AppMessageType.TEXT -> {
////                                    val length = reader.readInt()
////                                    val messageByte = ByteArray(length)
////                                    var end = false
////                                    val dataString = java.lang.StringBuilder(length)
////                                    var totalBytesRead = 0
////                                    while (!end) {
////                                        val currentBytesRead: Int = reader.read(messageByte)
////                                        totalBytesRead += currentBytesRead
////                                        if (totalBytesRead <= length) {
////                                            dataString.append(
////                                                    String(
////                                                        messageByte,
////                                                        0,
////                                                        currentBytesRead,
////                                                        StandardCharsets.UTF_8
////                                                    )
////                                                )
////                                        } else {
////                                            dataString.append(
////                                                    String(
////                                                        messageByte,
////                                                        0,
////                                                        length - totalBytesRead + currentBytesRead,
////                                                        StandardCharsets.UTF_8
////                                                    )
////                                                )
////                                        }
////                                        if (dataString.length >= length) {
////                                            end = true
////                                            log("host incoming message - $dataString")
////                                        }
////                                    }
////                                    //                            val inputData = reader.readUTF()
//////                            val message = gson.fromJson(
//////                                inputData,
//////                                Message::class.java
//////                            )
//////                            viewModel.insertMessage(message)
////                                }
////
////                                AppMessageType.FILE -> {
////                                    receiveFile(dataInputStream = reader, fileName = "4228.pdf")
////                                }
////
////                                AppMessageType.UNKNOWN -> {}
////                            }
////                        } catch (e: EOFException) {
////                            //if the IP address of the host could not be determined.
////                            Log.d("ahi3646", "createServer: EOFException")
////                            connectedClientSocketOnServer.close()
////                            viewModel.updateConnectionsCount(false)
////                            log("in while - ${connectedClientSocketOnServer.isClosed} - $connectedClientSocketOnServer")
////                            viewModel.handleEvents(
////                                TcpScreenEvents.OnDialogErrorOccurred(
////                                    TcpScreenDialogErrors.EOException
////                                )
////                            )
////                            //HERE IS THE POINT !!!
////                            try {
////                                reader.close()
////                            } catch (e: IOException) {
////                                e.printStackTrace()
////                            }
////                        } catch (e: IOException) {
////                            //the stream has been closed and the contained
////                            // input stream does not support reading after close,
////                            // or another I/O error occurs
////                            Log.d("ahi3646", "createServer: io exception ")
////                            viewModel.handleEvents(
////                                TcpScreenEvents.OnDialogErrorOccurred(
////                                    TcpScreenDialogErrors.IOException
////                                )
////                            )
////                            viewModel.updateConnectionsCount(false)
////                            connectedClientSocketOnServer.close()
////                            //serverSocket.close()
////                            try {
////                                reader.close()
////                            } catch (e: IOException) {
////                                e.printStackTrace()
////                                log("reader close exception - $e ")
////                            }
////                        } catch (e: UTFDataFormatException) {
////                            /** here is firing***/
////                            //if the bytes do not represent a valid modified UTF-8 encoding of a string.
////                            Log.d("ahi3646", "createServer: io exception ")
////                            viewModel.handleEvents(
////                                TcpScreenEvents.OnDialogErrorOccurred(
////                                    TcpScreenDialogErrors.UTFDataFormatException
////                                )
////                            )
////                            viewModel.updateConnectionsCount(false)
////                            connectedClientSocketOnServer.close()
////                            //serverSocket.close()
////                            try {
////                                reader.close()
////                            } catch (e: IOException) {
////                                e.printStackTrace()
////                            }
////                        }
//                    }
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//                try {
//                    viewModel.updateConnectionsCount(false)
//                } catch (ex: IOException) {
//                    ex.printStackTrace()
//                }
//                serverSocket.close()
//                //change server title status
//                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)
//
//            } catch (e: SecurityException) {
//                try {
//                    viewModel.updateConnectionsCount(false)
//                } catch (ex: IOException) {
//                    ex.printStackTrace()
//                }
//                serverSocket.close()
//                //change server title status
//                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)
//
//                //if a security manager exists and its checkConnect method doesn't allow the operation.
//                Log.d("ahi3646", "createServer: SecurityException ")
//            } catch (e: IllegalArgumentException) {
//                try {
//                    viewModel.updateConnectionsCount(false)
//                } catch (ex: IOException) {
//                    ex.printStackTrace()
//                }
//                serverSocket.close()
//                //change server title status
//                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)
//
//                //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
//                Log.d("ahi3646", "createServer: IllegalArgumentException ")
//            }
        }
    }

    private fun connectToServer(serverIpAddress: String, serverPort: Int) {
        log("connecting to server - $serverIpAddress:$serverPort")
        try {
            //create client
            clientSocket = Socket(serverIpAddress, serverPort)

            viewModel.updateClientConnectionStatus(ClientConnectionStatus.Connected)
            viewModel.updateConnectionsCount(true)
            log("client socket - $clientSocket")

            //received outcome messages here
            while (!clientSocket.isClosed) {
                val reader = DataInputStream(BufferedInputStream(clientSocket.getInputStream()))

                try {
                    val dataType = AppMessageType.fromChar(reader.readChar())
                    log("incoming message type - $dataType")

                    when (dataType) {
                        AppMessageType.INITIAL -> {}
                        AppMessageType.TEXT -> {
                            val receivedMessage = reader.readUTF()
                            log("host incoming message - $receivedMessage")

                            val currentTime = Calendar.getInstance().time
                            val message = Message.TextMessage(
                                username = "from client",
                                formattedTime = currentTime.toString(),
                                message = receivedMessage.toString(),
                                isFromYou = false
                            )
                            viewModel.insertMessage(message)
                        }

                        AppMessageType.FILE -> {}
                        AppMessageType.UNKNOWN -> {}
                    }
                } catch (e: EOFException) {
                    //if the IP address of the host could not be determined.
                    Log.d("ahi3646", "connectToServer: EOFException ")
                    viewModel.handleEvents(
                        TcpScreenEvents.OnDialogErrorOccurred(
                            TcpScreenDialogErrors.EOException
                        )
                    )

                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } catch (e: IOException) {
                    //the stream has been closed and the contained
                    // input stream does not support reading after close,
                    // or another I/O error occurs
                    Log.d("ahi3646", "connectToServer: io exception ")
                    viewModel.handleEvents(
                        TcpScreenEvents.OnDialogErrorOccurred(
                            TcpScreenDialogErrors.IOException
                        )
                    )

                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } catch (e: UTFDataFormatException) {
                    //if the bytes do not represent a valid modified UTF-8 encoding of a string.
                    Log.d("ahi3646", "connectToServer: io exception ")
                    viewModel.handleEvents(
                        TcpScreenEvents.OnDialogErrorOccurred(
                            TcpScreenDialogErrors.UTFDataFormatException
                        )
                    )
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (exception: UnknownHostException) {
            log("unknown host exception".uppercase())
            try {
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.UnknownHostException))
        } catch (exception: IOException) {
            try {
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //could not connect to a server
            log("connectToServer: IOException ".uppercase())
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.IOException))
        } catch (e: SecurityException) {
            try {
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //if a security manager exists and its checkConnect method doesn't allow the operation.
            log("connectToServer: SecurityException".uppercase())
        } catch (e: IllegalArgumentException) {
            try {
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
            log("connectToServer: IllegalArgumentException".uppercase())
        }

    }

    private fun sendClientMessage(message: Message) {
        if (!clientSocket.isClosed) {
            val writer = DataOutputStream(clientSocket.getOutputStream())
            when (message) {
                is Message.TextMessage -> {
                    log("sending text message from client - $message")

                    val type = AppMessageType.TEXT.identifier
                    val data = message.message

                    try {
                        writer.writeChar(type.code)
                        writer.writeUTF(data)
                        viewModel.handleEvents(TcpScreenEvents.InsertMessage(message))
                    } catch (e: IOException) {
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

                is Message.FileMessage -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendFile(
                            writer = writer,
                            fileMessage = message
                        )
                    }
                }
            }
        } else {
            Log.d("ahi3646", "send client message: client socket is closed ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
        }
    }

    private fun sendHostMessage(message: Message) {
        if (!connectedClientSocketOnServer.isClosed) {
            val writer = DataOutputStream(connectedClientSocketOnServer.getOutputStream())

            when (message) {
                is Message.TextMessage -> {
                    log("sending text message from host - $message")

                    val type = AppMessageType.TEXT.identifier
                    val data = message.message

                    try {
                        writer.writeChar(type.code)
                        writer.writeUTF(data)
                        viewModel.handleEvents(TcpScreenEvents.InsertMessage(message))
                    } catch (e: IOException) {
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

                is Message.FileMessage -> {
                    log("sending file message - $message")
                }
            }
        } else {
            Log.d("ahi3646", "send host message: client socket is closed ")
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
        receiver = WiFiDirectBroadcastReceiver(wifiP2pManager = wifiP2PManager,
            channel = channel,
            peerListListener = peerListListener,
            networkEventHandler = { networkEvent ->
                viewModel.handleNetworkEvents(networkEvent)
            })
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


