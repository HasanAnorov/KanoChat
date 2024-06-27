package com.ierusalem.androchat.features_tcp.tcp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.navigation.emitNavigation
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.executeWithLifecycle
import com.ierusalem.androchat.core.utils.log
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.UTFDataFormatException
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException


@AndroidEntryPoint
class TcpFragment : Fragment() {

    private val viewModel: TcpViewModel by viewModels()

    //wifi direct
    private lateinit var wifiP2PManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private val intentFilter = IntentFilter()

    //permission
    private lateinit var permissionGuard: PermissionGuardImpl

    //gson to convert message object to string
    private lateinit var gson: Gson

    //chatting server side
    private lateinit var serverSocket: ServerSocket
    private lateinit var connectedClientSocketOnServer: Socket

    //chatting client side
    private lateinit var clientSocket: Socket

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gson = Gson()
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        permissionGuard = PermissionGuardImpl(requireContext())
        wifiP2PManager =
            requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

        channel = wifiP2PManager
            .initialize(
                requireContext(),
                Looper.getMainLooper()
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

                //todo
//                BackHandler {
//                    if (state.messages.isNotEmpty()) {
//                        //show close dialog here
//                    }
//                }

                AndroChatTheme {
                    TcpScreen(
                        state = state,
                        eventHandler = { viewModel.handleEvents(it) },
                        allTabs = allTabs,
                        pagerState = pagerState,
                        onTabChanged = { handleTabSelected(it) }
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.screenNavigation.executeWithLifecycle(
            lifecycle = viewLifecycleOwner.lifecycle,
            action = ::executeNavigation
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
        val listener =
            object : WifiP2pManager.ActionListener {
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
                channel,
                getConfiguration(),
                listener
            )
        } else {
            log("Creating group1")
            wifiP2PManager.createGroup(
                channel,
                listener
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
        return WifiP2pConfig.Builder()
            .setNetworkName(ssid)
            .setPassphrase(passwd)
            .setGroupOperatingBand(band)
            .build()
    }

    private fun stopHotspotNetworking() {
        //close socket only when serverSocket is initialized
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
        if(::connectedClientSocketOnServer.isInitialized){
            connectedClientSocketOnServer.close()
        }

        wifiP2PManager.removeGroup(
            channel,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    log("Wifi P2P Channel is removed")
                    viewModel.updateHotspotDiscoveryStatus(HotspotNetworkingStatus.Idle)
                }

                override fun onFailure(reason: Int) {
                    val r = Reason.parseReason(reason)
                    log("Failed to stop network: ${r.displayReason}")
                }
            }
        )
    }

    private fun stopPeerDiscovery() {
        val listener =
            object : WifiP2pManager.ActionListener {
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
            channel,
            listener
        )
    }

    @SuppressLint("MissingPermission")
    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

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

            is TcpScreenNavigation.OnCreateServerClick -> {
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
                    requireContext(),
                    navigation.tcpScreenErrors.errorMessage,
                    Toast.LENGTH_SHORT
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

                        wifiP2PManager.discoverPeers(
                            channel, object : WifiP2pManager.ActionListener {
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

                            }
                        )
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

    private suspend fun createServer(serverPort: Int) {
        log("creating server")
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
                    Log.d("ahi3646", "New client : $connectedClientSocketOnServer ")
                    viewModel.updateConnectionsCount(true)
                    while (!connectedClientSocketOnServer.isClosed) {
                        val reader = DataInputStream(connectedClientSocketOnServer.getInputStream())
                        try {
                            val inputData = reader.readUTF()
                            val message = gson.fromJson(
                                inputData,
                                Message::class.java
                            ) //todo use inputData.toMessage(gson)
                            viewModel.insertMessage(message)
                            Log.d("ahi3646", "createServer: $message ")
                        } catch (e: EOFException) {
                            //if the IP address of the host could not be determined.
                            Log.d("ahi3646", "createServer: EOFException")
                            connectedClientSocketOnServer.close()
                            viewModel.updateConnectionsCount(false)
                            log("in while - ${connectedClientSocketOnServer.isClosed} - $connectedClientSocketOnServer")
                            viewModel.handleEvents(
                                TcpScreenEvents.OnDialogErrorOccurred(
                                    TcpScreenDialogErrors.EOException
                                )
                            )

                            //HERE IS THE POINT !!!
                            try {
                                reader.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } catch (e: IOException) {
                            //the stream has been closed and the contained
                            // input stream does not support reading after close,
                            // or another I/O error occurs
                            Log.d("ahi3646", "createServer: io exception ")
                            viewModel.handleEvents(
                                TcpScreenEvents.OnDialogErrorOccurred(
                                    TcpScreenDialogErrors.IOException
                                )
                            )
                            viewModel.updateConnectionsCount(false)
                            connectedClientSocketOnServer.close()
                            //serverSocket.close()
                            try {
                                reader.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                                log("reader close exception - $e ")
                            }
                        } catch (e: UTFDataFormatException) {
                            //if the bytes do not represent a valid modified UTF-8 encoding of a string.
                            Log.d("ahi3646", "createServer: io exception ")
                            viewModel.handleEvents(
                                TcpScreenEvents.OnDialogErrorOccurred(
                                    TcpScreenDialogErrors.UTFDataFormatException
                                )
                            )
                            viewModel.updateConnectionsCount(false)
                            connectedClientSocketOnServer.close()
                            //serverSocket.close()
                            try {
                                reader.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                try {
                    viewModel.updateConnectionsCount(false)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                serverSocket.close()
                //change server title status
                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)

            } catch (e: SecurityException) {
                try {
                    viewModel.updateConnectionsCount(false)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                serverSocket.close()
                //change server title status
                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)

                //if a security manager exists and its checkConnect method doesn't allow the operation.
                Log.d("ahi3646", "createServer: SecurityException ")
            } catch (e: IllegalArgumentException) {
                try {
                    viewModel.updateConnectionsCount(false)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                serverSocket.close()
                //change server title status
                viewModel.updateHostConnectionStatus(HostConnectionStatus.Failure)

                //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
                Log.d("ahi3646", "createServer: IllegalArgumentException ")
            }
        }
    }

    private fun connectToServer(serverIpAddress: String, serverPort: Int) {
        log("connecting to server - $serverIpAddress:$serverPort")
        try {
            //create client
            log("CLIENT CREATION TRY")
            clientSocket = Socket(serverIpAddress, serverPort)

            viewModel.updateClientConnectionStatus(ClientConnectionStatus.Connected)
            viewModel.updateConnectionsCount(true)
            log("client socket - $clientSocket")

            //received outcome messages here
            while (!clientSocket.isClosed) {
                log("while client socket is running")
                val reader = DataInputStream(clientSocket.getInputStream())
                try {
                    log("client while input reading try")
                    val inputData = reader.readUTF()
                    val message = gson.fromJson(inputData, Message::class.java)
                    viewModel.insertMessage(message)
                    Log.d("ahi3646", "connectToServer: inputData - $inputData ")
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
            try {
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            Log.d("ahi3646", "connectToServer: UnknownHostException ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.UnknownHostException))
        } catch (exception: IOException) {
            try {
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //could not connect to a server
            Log.d("ahi3646", "connectToServer: IOException ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.IOException))
        } catch (e: SecurityException) {
            try {
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //if a security manager exists and its checkConnect method doesn't allow the operation.
            Log.d("ahi3646", "connectToServer: SecurityException ")
        } catch (e: IllegalArgumentException) {
            try {
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
            Log.d("ahi3646", "connectToServer: IllegalArgumentException ")
        }

    }

    private fun sendClientMessage(message: Message) {
        if (!clientSocket.isClosed) {
            val writer = DataOutputStream(clientSocket.getOutputStream())

            val messageStringForms = gson.toJson(message)
            Log.d("ahi3646", "sendMessage: $messageStringForms ")

            try {
                writer.writeUTF(messageStringForms)
                writer.flush()
                viewModel.handleEvents(TcpScreenEvents.InsertMessage(message))
            } catch (exception: IOException) {
                Log.d("ahi3646", "sendMessage: io exception ")
                viewModel.handleEvents(
                    TcpScreenEvents.OnDialogErrorOccurred(
                        TcpScreenDialogErrors.IOException
                    )
                )
                try {
                    Log.d(
                        "ahi3646",
                        "sendMessage client: dataOutputStream is closed io exception "
                    )
                    writer.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        } else {
            Log.d("ahi3646", "sendMessage: client socket is closed ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
        }
    }

    private fun sendHostMessage(message: Message) {
        log("in send message - ${connectedClientSocketOnServer.isClosed} - $connectedClientSocketOnServer")
        if (!connectedClientSocketOnServer.isClosed) {
            val writer = DataOutputStream(connectedClientSocketOnServer.getOutputStream())

            val messageStringForms = gson.toJson(message)
            Log.d("ahi3646", "sendMessage: $messageStringForms ")

            try {
                writer.writeUTF(messageStringForms)
                writer.flush()
                viewModel.handleEvents(TcpScreenEvents.InsertMessage(message))
            } catch (e: IOException) {
                viewModel.handleEvents(
                    TcpScreenEvents.OnDialogErrorOccurred(
                        TcpScreenDialogErrors.IOException
                    )
                )
                try {
                    Log.d(
                        "ahi3646",
                        "sendMessage server: dataOutputStream is closed io exception "
                    )
                    writer.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        } else {
            Log.d("ahi3646", "sendMessage: client socket is closed ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.EstablishConnectionToSendMessage))
        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        if(::connectedClientSocketOnServer.isInitialized){
            connectedClientSocketOnServer.close()
        }
        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
        if (::clientSocket.isInitialized) {
            clientSocket.close()
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(receiver)
    }

}