package com.ierusalem.androchat.features_tcp.tcp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenNavigation
import com.ierusalem.androchat.features_tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.domain.ClientStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.ServerStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_tcp.tcp.domain.WifiDiscoveryStatus
import com.ierusalem.androchat.features_tcp.tcp.presentation.components.rememberAllTabs
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
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@AndroidEntryPoint
class TcpFragment : Fragment() {

    private val viewModel: TcpViewModel by viewModels()

    //wifi direct
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var receiver: WiFiDirectBroadcastReceiver

    //server
    private lateinit var serverSelectorManager: SelectorManager
    private lateinit var serverSocket: ServerSocket

    //client
    private lateinit var clientSelectorManager: SelectorManager
    private lateinit var clientSocket: Socket

    private val intentFilter = IntentFilter()

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

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        wifiP2pManager =
            requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = createChannel()
        intentFilter.apply {
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                val scope = rememberCoroutineScope()
                val allTabs = rememberAllTabs()
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0F,
                    pageCount = { allTabs.size },
                )

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

                val state by viewModel.state.collectAsStateWithLifecycle()

                AndroChatTheme {
                    TcpScreen(
                        eventHandler = {
                            viewModel.handleEvents(it)
                        },
                        allTabs = allTabs,
                        pagerState = pagerState,
                        onTabChanged = { handleTabSelected(it) },
                        state = state
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

    private fun createChannel(): WifiP2pManager.Channel {
        Log.d("ahi3646", "creating channel ")
        return wifiP2pManager
            .initialize(
                requireContext(),
                Looper.getMainLooper()
            ) {
                Log.d("ahi3646", "WifiP2PManager Channel died! Do nothing :D")
            }
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        Log.d("ahi3646", "peersList - $peerList")
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

    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(
            wifiP2pManager = wifiP2pManager,
            channel = channel,
            onWifiEnabled = {
                viewModel.handleEvents(TcpScreenEvents.OnWifiStateChanged(it))
            },
            listener = peerListListener
        )
        requireActivity().registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(receiver)
    }


    @SuppressLint("MissingPermission")
    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {
            TcpScreenNavigation.OnCreateWiFiClick -> {
                //todo
            }

            TcpScreenNavigation.OnDiscoverWifiClick -> {
                val permissionGuard = PermissionGuardImpl(requireContext())
                lifecycleScope.launch {
                    if (permissionGuard.canCreateNetwork()) {
                        Log.d("ahi3646", "permission granted: ")
                        wifiP2pManager.discoverPeers(
                            channel, object : WifiP2pManager.ActionListener {
                                override fun onSuccess() {
                                    Log.d("ahi3646", "onSuccess: discover ")
                                    viewModel.updateWifiDiscoveryStatus(WifiDiscoveryStatus.Discovering)
                                }

                                override fun onFailure(reason: Int) {
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

            TcpScreenNavigation.OnNavIconClick -> findNavController().popBackStack()

            TcpScreenNavigation.OnSettingsClick -> {

            }

            TcpScreenNavigation.OnCloseServerClick -> {
                serverSocket.close()
                viewModel.updateHotspotTitleStatus(ServerStatus.Idle)
            }

            TcpScreenNavigation.OnDisconnectServerClick -> {
                clientSocket.close()
                viewModel.updateClientTitleStatus(ClientStatus.Idle)
            }

            is TcpScreenNavigation.OnCreateServerClick -> {
                CoroutineScope(Dispatchers.Default).launch {
                    openHotspot(
                        hotspotName = navigation.hotspotName,
                        hotspotPassword = navigation.hotspotPassword,
                        port = navigation.portNumber
                    )
                }
            }

            is TcpScreenNavigation.OnConnectToServerClick -> {
                CoroutineScope(Dispatchers.Default).launch {
                    connectToServer()
                }
            }
        }
    }


    @OptIn(InternalAPI::class)
    private suspend fun connectToServer() {
        clientSelectorManager = SelectorManager(Dispatchers.IO)
        clientSocket = aSocket(clientSelectorManager).tcp().connect("127.0.0.1", 9002)
        Log.d("ahi3646", "connectToServer ip address: ${clientSocket.localAddress} ")

        val receiveChannel = clientSocket.openReadChannel()
        val sendChannel = clientSocket.openWriteChannel(autoFlush = true)

        withContext(Dispatchers.IO) {
            while (true) {
                val greeting = receiveChannel.readUTF8Line()
                if (greeting != null) {
                    Log.d("ahi3646", greeting)
                } else {
                    Log.d("ahi3646", "Server closed a connection")
                    clientSocket.close()
                    clientSelectorManager.close()
                    exitProcess(0)
                }
            }
        }

    }

    @OptIn(InternalAPI::class)
    private suspend fun openHotspot(hotspotName: String, hotspotPassword: String, port: Int) {
        Log.d(
            "ahi3646",
            "openHotspot: " +
                    "\nhotspotName - $hotspotName" +
                    "\nhotspotPassword - $hotspotPassword" +
                    "\nport - $port"
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
                .bind("127.0.0.1", 9002)


            Log.d("ahi3646", "Server is listening at ${serverSocket.localAddress}")
            viewModel.updateHotspotTitleStatus(ServerStatus.Created)

            while (true) {
                //Accept incoming connections
                val socket = serverSocket.accept()
                Log.d("ahi3646", "Socket Accepted $socket")

                launch {
                    //Receive data
                    val receiveChannel = socket.openReadChannel()
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    sendChannel.writeStringUtf8("Please enter your name\n")
                }
            }
        }
    }

}