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
import androidx.activity.compose.BackHandler
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
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiNetworkEvent
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenEvents
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenNavigation
import com.ierusalem.androchat.features_tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.domain.ClientConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.ConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.OwnerStatusState
import com.ierusalem.androchat.features_tcp.tcp.domain.HostConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenDialogErrors
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenErrors
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_tcp.tcp.domain.WifiDiscoveryStatus
import com.ierusalem.androchat.features_tcp.tcp.presentation.components.rememberTcpAllTabs
import com.ierusalem.androchat.ui.navigation.emitNavigation
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.executeWithLifecycle
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
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private val intentFilter = IntentFilter()

    //permission
    private lateinit var permissionGuard: PermissionGuardImpl

    //gson to convert message object to string
    private lateinit var gson: Gson

    //chatting server side
    private lateinit var serverSocket: ServerSocket
    private lateinit var serverAcceptedSocket: Socket

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

                BackHandler {
                    if(state.messages.isNotEmpty()){
                        //show close dialog here
                    }
                }

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

    private fun handleWifiDisabledCase(status: OwnerStatusState) {
        when (status) {
            OwnerStatusState.Idle -> {
                //do nothing
            }

            OwnerStatusState.Client -> {
                clientSocket.close()
            }

            OwnerStatusState.Owner -> {
                serverAcceptedSocket.close()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {

            TcpScreenNavigation.OnNavIconClick -> {
                //findNavController().popBackStack()
            }

            is TcpScreenNavigation.WifiDisabledCase -> {
                handleWifiDisabledCase(navigation.status)
            }

            TcpScreenNavigation.OnSettingsClick -> {
                findNavController().navigate(R.id.action_tcpFragment_to_tcpSettingFragment)
            }

            is TcpScreenNavigation.OnCreateServerClick -> {
                CoroutineScope(Dispatchers.Default).launch {
                    createServer(serverPort = navigation.portNumber)
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

    //all exceptions handled
    private suspend fun createServer(serverPort: Int) {
        withContext(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(serverPort)
                Log.d("ahi3646", "createServer: $serverSocket ${serverSocket.localSocketAddress} ")
                if (serverSocket.isBound) {
                    viewModel.handleEvents(TcpScreenEvents.UpdateServerStatus(HostConnectionStatus.Created))
                }
                while (!serverSocket.isClosed) {
                    serverAcceptedSocket = serverSocket.accept()
                    viewModel.updateConnectionsCount(true)
                    Log.d("ahi3646", "New client : $serverAcceptedSocket ")
                    while (!serverAcceptedSocket.isClosed) {
                        val reader = DataInputStream(serverAcceptedSocket.getInputStream())
                        try {
                            val inputData = reader.readUTF()
                            val message = gson.fromJson(inputData, Message::class.java)
                            viewModel.insertMessage(message)
                            Log.d("ahi3646", "createServer: $message ")
                        } catch (e: EOFException) {
                            //if the IP address of the host could not be determined.
                            Log.d("ahi3646", "createServer: EOFException ")
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
                            Log.d("ahi3646", "createServer: io exception ")
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
                            Log.d("ahi3646", "createServer: io exception ")
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
                }
            } catch (e: IOException) {
                e.printStackTrace()
                try {
                    serverAcceptedSocket.close()
                    viewModel.updateConnectionsCount(false)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            } catch (e: SecurityException) {
                try {
                    serverAcceptedSocket.close()
                    viewModel.updateConnectionsCount(false)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                //if a security manager exists and its checkConnect method doesn't allow the operation.
                Log.d("ahi3646", "createServer: SecurityException ")
            } catch (e: IllegalArgumentException) {
                try {
                    serverAcceptedSocket.close()
                    viewModel.updateConnectionsCount(false)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
                Log.d("ahi3646", "createServer: IllegalArgumentException ")
            }
        }
    }

    //all exceptions handled
    private fun connectToServer(serverIpAddress: String, serverPort: Int) {
        try {
            //create client
            clientSocket = Socket(serverIpAddress, serverPort)
            //update client title status
            if (!clientSocket.isClosed) {
                viewModel.handleEvents(TcpScreenEvents.UpdateClientStatus(ClientConnectionStatus.Created))
                viewModel.updateConnectionsCount(true)
            }

            //received outcome messages here
            while (!clientSocket.isClosed) {
                val reader = DataInputStream(clientSocket.getInputStream())
                try {
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
                clientSocket.close()
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            Log.d("ahi3646", "connectToServer: UnknownHostException ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.UnknownHostException))
        } catch (exception: IOException) {
            try {
                clientSocket.close()
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            Log.d("ahi3646", "connectToServer: IOException ")
            viewModel.handleEvents(TcpScreenEvents.OnDialogErrorOccurred(TcpScreenDialogErrors.IOException))
        } catch (e: SecurityException) {
            try {
                clientSocket.close()
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //if a security manager exists and its checkConnect method doesn't allow the operation.
            Log.d("ahi3646", "connectToServer: SecurityException ")
        } catch (e: IllegalArgumentException) {
            try {
                clientSocket.close()
                //todo change connection status here
                viewModel.updateConnectionsCount(false)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            //if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.
            Log.d("ahi3646", "connectToServer: IllegalArgumentException ")
        }

    }

    private fun sendClientMessage(message: Message){
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

    private fun sendHostMessage(message: Message){
        if (!serverAcceptedSocket.isClosed) {
            val writer = DataOutputStream(serverAcceptedSocket.getOutputStream())

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