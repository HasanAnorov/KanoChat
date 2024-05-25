package com.ierusalem.androchat.features_tcp.tcp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiNetworkEvent
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_tcp.service.TcpServerService
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenNavigation
import com.ierusalem.androchat.features_tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.domain.ConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.ServerStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenErrors
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_tcp.tcp.domain.WifiDiscoveryStatus
import com.ierusalem.androchat.features_tcp.tcp.presentation.components.rememberTcpAllTabs
import com.ierusalem.androchat.ui.navigation.emitNavigation
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.executeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.InternalAPI
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TcpFragment : Fragment() {

    private val viewModel: TcpViewModel by viewModels()

    //wifi direct
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private val intentFilter = IntentFilter()

    //server
    private lateinit var serverSelectorManager: SelectorManager
    private lateinit var serverSocket: ServerSocket
    private lateinit var serverWriteChannel: ByteWriteChannel
    private lateinit var serverReadChannel: ByteReadChannel

    //you can't send message with serverSocket
    private lateinit var serverConnectedSocket: Socket

    //client
    private lateinit var clientSelectorManager: SelectorManager
    private lateinit var clientSocket: Socket
    private lateinit var clientWriteChannel: ByteWriteChannel
    private lateinit var clientReadChannel: ByteReadChannel

    //permission
    private lateinit var permissionGuard: PermissionGuardImpl

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(
                Intent(
                    requireContext(),
                    TcpServerService::class.java
                )
            )
        } else {
            requireActivity().startService(Intent(requireContext(), TcpServerService::class.java))
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        permissionGuard = PermissionGuardImpl(requireContext())
        wifiP2pManager = requireContext()
            .getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager
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

    @SuppressLint("MissingPermission")
    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

            TcpScreenNavigation.OnNavIconClick -> {
                findNavController().popBackStack()
            }

            TcpScreenNavigation.OnSettingsClick -> {
                findNavController().navigate(R.id.action_tcpFragment_to_tcpSettingFragment)
            }

            is TcpScreenNavigation.OnCreateServerClick -> {
                CoroutineScope(Dispatchers.Default).launch {
                    createServer(
                        serverAddress = navigation.serverIpAddress,
                        serverPort = navigation.portNumber
                    )
                }
            }

            is TcpScreenNavigation.OnConnectToServerClick -> {
                CoroutineScope(Dispatchers.Default).launch {
                    connectToServer(
                        serverIpAddress = navigation.serverIpAddress,
                        serverPort = navigation.portNumber
                    )
                }
            }

            TcpScreenNavigation.OnCloseServerClick -> {
                serverSocket.close()
                viewModel.updateHotspotTitleStatus(ServerStatus.Idle)
            }

            TcpScreenNavigation.OnDisconnectServerClick -> {
                clientSocket.close()
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

            TcpScreenNavigation.OnDiscoverWifiClick -> {
                lifecycleScope.launch {
                    if (permissionGuard.canCreateNetwork()) {
                        wifiP2pManager.discoverPeers(
                            channel, object : WifiP2pManager.ActionListener {
                                override fun onSuccess() {
                                    Log.d("ahi3646", "onSuccess: discover ")
                                    //todo optimize this
                                    viewModel.handleNetworkEvents(
                                        WiFiNetworkEvent.ConnectionStatusChanged(
                                            ConnectionStatus.Running
                                        )
                                    )
                                    viewModel.updateWifiDiscoveryStatus(WifiDiscoveryStatus.Discovering)
                                }

                                override fun onFailure(reason: Int) {
                                    // Code for when the discovery initiation fails goes here.
                                    // Alert the user that something went wrong.
                                    Log.d("ahi3646", "onFailure: discover $reason ")
                                    viewModel.updateWifiDiscoveryStatus(WifiDiscoveryStatus.Failure)
                                }

                            }
                        )
                    } else {
                        locationPermissionRequest.launch(
                            permissionGuard.requiredPermissions.toTypedArray()
                        )
                        Log.d("ahi3646", "request permission: ")
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
        wifiP2pManager.connect(
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

    @OptIn(InternalAPI::class)
    private suspend fun createServer(serverAddress: String, serverPort: Int) {
        Log.d(
            "ahi3646",
            "openHotspot: " +
                    "\nhotspotPassword - $serverAddress" +
                    "\nport - $serverPort"
        )

        runBlocking {
            //Create a server socket
            serverSelectorManager = SelectorManager(Dispatchers.IO)
            serverSocket = aSocket(serverSelectorManager)
                .tcp()
                //.configure {
                //todo think about these
                //reuseAddress = true
                //reusePort = true
                //}
                .bind(serverAddress, serverPort)


            Log.d("ahi3646", "Server is listening at ${serverSocket.localAddress}")
            viewModel.updateHotspotTitleStatus(ServerStatus.Created)

//            while (true) {
            //Accept incoming connections
            val socket = serverSocket.accept()
            Log.d("ahi3646", "Socket Accepted $socket")
            //Receive data
            //val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            repeat(3) {
                launch {
                    sendChannel.writeStringUtf8("Please enter your name\n")
                }
                delay(3000)
                Log.d("ahi3646", "createServer: repeat ")
            }
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun connectToServer(serverIpAddress: String, serverPort: Int) {
        clientSelectorManager = SelectorManager(Dispatchers.IO)
        clientSocket = aSocket(clientSelectorManager).tcp().connect(serverIpAddress, serverPort)
        Log.d(
            "ahi3646",
            "connectToServer ip address: $serverIpAddress $serverPort ${clientSocket.localAddress} "
        )

        val receiveChannel = clientSocket.openReadChannel()
        val sendChannel = clientSocket.openWriteChannel(autoFlush = true)

        withContext(Dispatchers.IO) {
            while (true) {
                val greeting = receiveChannel.readUTF8Line()
                if (greeting != null) {
                    Log.d("ahi3646", greeting)
                }
//                else {
//                    Log.d("ahi3646", "Server closed a connection")
//                    clientSocket.close()
//                    clientSelectorManager.close()
//                    exitProcess(0)
//                }
            }
        }
    }

//    private fun sendMessages(message: String) {
//        if (viewModel.state.value.isOwner == OwnerStatusState.Owner) {
//            lifecycleScope.launch(Dispatchers.IO) {
//                try {
//                    //todo need to clarify
//                    while (true) {
//                        serverWriteChannel.writeStringUtf8(message)
//                    }
//                } catch (e: Throwable) {
//                    Log.d("ahi3646", "sendMessages: error $e ")
//                    serverConnectedSocket.close()
//                }
//            }
//        } else {
//            lifecycleScope.launch(Dispatchers.IO) {
//                try {
//                    //todo need to clarify
//                    while (true) {
//                        clientWriteChannel.writeStringUtf8(message)
//                    }
//                } catch (e: Throwable) {
//                    Log.d("ahi3646", "sendMessages: error $e ")
//                    Log.d("ahi3646", "Server closed a connection")
//                    clientSocket.close()
//                    clientSelectorManager.close()
//                    exitProcess(0)
//                }
//            }
//        }
//    }
//
//    private fun readMessages() {
//        if (viewModel.state.value.isOwner == OwnerStatusState.Owner) {
//            lifecycleScope.launch(Dispatchers.IO) {
//                try {
//                    while (true) {
//                        val incomingMessage = serverReadChannel.readUTF8Line()
//                        if (incomingMessage != null) {
//                            viewModel.handleEvents(TcpScreenEvents.SendMessage(incomingMessage))
//                        }
//                    }
//                } catch (e: Throwable) {
//                    Log.d("ahi3646", "readMessages: error $e ")
//                    serverConnectedSocket.close()
//                }
//            }
//        } else {
//            lifecycleScope.launch(Dispatchers.IO) {
//                while (true) {
//                    val incomingMessage = clientReadChannel.readUTF8Line()
//                    if (incomingMessage != null) {
//                        viewModel.handleEvents(TcpScreenEvents.SendMessage(incomingMessage))
//                    } else {
//                        Log.d("ahi3646", "Server closed a connection")
//                        clientSocket.close()
//                        clientSelectorManager.close()
//                        exitProcess(0)
//                    }
//                }
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(
            wifiP2pManager = wifiP2pManager,
            channel = channel,
            peerListListener = peerListListener,
            networkEventHandler = { networkEvent ->
                viewModel.handleNetworkEvents(networkEvent)
            }
        )
        requireActivity().registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(receiver)
    }

}