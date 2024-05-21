package com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.util.Log
import com.ierusalem.androchat.features_tcp.server.broadcast.BroadcastEvent
import com.ierusalem.androchat.features_tcp.server.broadcast.BroadcastObserver
import com.ierusalem.androchat.utils.EventBus
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WiFiDirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager?,
    private val channel: Channel,
    private val onWifiEnabled: (Boolean) -> Unit,
    private val listener: WifiP2pManager.PeerListListener
) : BroadcastReceiver(), BroadcastObserver {

    private val receiverScope by lazy {
        CoroutineScope(
            context = SupervisorJob() + Dispatchers.Default + CoroutineName(this::class.java.name),
        )
    }

    private val eventBus = EventBus()

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        // Go async in case scope work takes a long time
        Log.d("ahi3646", "onReceive: ${intent.action} ")
        val pending = goAsync()

        // Use Default here instead of ProxyDispatcher
        receiverScope.launch(context = Dispatchers.Default) {
            try {
                when (val action = intent.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> handleStateChangedAction(intent)
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        Log.d("ahi3646", "connection changed action: ")
                        handleConnectionChangedAction(intent)
                    }
                    WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                        Log.d("ahi3646", "handleDiscoveryChangedAction: ")
                        handleDiscoveryChangedAction(intent)
                    }
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        Log.d("ahi3646", "onReceive: peers list have changed ")
//                        handlePeersChangedAction(intent)
                        wifiP2pManager?.requestPeers(channel, listener)
                    }
                    WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                        handleThisDeviceChangedAction(intent)
                        Log.d("ahi3646", "handleThisDeviceChangedAction: ")
                    }

                    else -> {
                        Log.d("ahi3646", "onReceive: Unhandled intent action: $action")
                    }
                }
            } finally {
                withContext(context = Dispatchers.Main) {
                    // Mark BR as finished
                    pending.finish()
                }
            }
        }
    }

    private suspend fun handleStateChangedAction(intent: Intent) {
        when (val p2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, 0)) {
            WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                Log.d("ahi3646", "handleStateChangedAction: WiFi Direct: Enabled")
                eventBus.emitEvent(WiFiNetworkEvent.WifiEnabled)
                onWifiEnabled(true)
            }

            WifiP2pManager.WIFI_P2P_STATE_DISABLED -> {
                Log.d("ahi3646", "handleStateChangedAction: WiFi Direct: Disabled")
                eventBus.emitEvent(WiFiNetworkEvent.WifiDisabled)
                onWifiEnabled(false)
                // Fire the shutdown event to the service
                // The service shutdown will properly clean up things like this WDN, as well as wakelocks
                // and notifications
                //todo think about this later
                //shutdownBus.emit(ServerShutdownEvent)
            }

            else -> Log.d("ahi3646", "handleStateChangedAction: Unknown Wifi p2p state: $p2pState")
        }
    }

    override suspend fun listenNetworkEvents(scope: CoroutineScope): Flow<BroadcastEvent> {
        return eventBus.getAsBroadcastEvent()
    }

    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->

        // String from WifiP2pInfo struct
        val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress.orEmpty()
        Log.d("ahi3646", "groupOwnerAddress: $groupOwnerAddress ")

        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
            Log.d("ahi3646", ": host ")
        } else if (info.groupFormed) {
            Log.d("ahi3646", ": client ")
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }
    }

    private fun handleConnectionChangedAction(intent: Intent) {
//        val hostName = resolveWifiGroupHostname(intent)
//        if (hostName.isNotBlank()) {
//            eventBus.emitEvent(WiFiNetworkEvent.ConnectionChanged(hostName))
//        }
        wifiP2pManager?.let { manager ->
            val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
            if(networkInfo?.isConnected == true){
                manager.requestConnectionInfo(channel, connectionListener )
            }
        }
    }

    private suspend fun handleDiscoveryChangedAction(intent: Intent) {
        eventBus.emitEvent(WiFiNetworkEvent.DiscoveryChanged)
    }

    private suspend fun handlePeersChangedAction(intent: Intent) {
        eventBus.emitEvent(WiFiNetworkEvent.PeersChanged)
    }

    private suspend fun handleThisDeviceChangedAction(intent: Intent) {
        eventBus.emitEvent(WiFiNetworkEvent.ThisDeviceChanged)
    }

}
