package com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.util.Log
import com.ierusalem.androchat.features_tcp.tcp.domain.ConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.OwnerStatusState

class WiFiDirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager?,
    private val channel: Channel,
    private val peerListListener: WifiP2pManager.PeerListListener,
    private val networkEventHandler: (WiFiNetworkEvent) -> Unit,
) : BroadcastReceiver() {

    private fun handleConnectionChangedAction(intent: Intent) {
        if (wifiP2pManager != null) {
            val networkInfo: NetworkInfo? =
                intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
            Log.d("ahi3646", "handleConnectionChangedAction: networkInfo - $networkInfo ")
            if (networkInfo?.isConnected == true) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                networkEventHandler(WiFiNetworkEvent.ConnectionStatusChanged(ConnectionStatus.Connected))
                wifiP2pManager.requestConnectionInfo(channel, connectionListener)
            } else {
                Log.d("ahi33646", "onReceive: network info not connected ")
            }
        } else {
            networkEventHandler(WiFiNetworkEvent.ConnectionStatusChanged(ConnectionStatus.Disconnected))
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (val action = intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> handleStateChangedAction(intent)

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                Log.d("ahi3646", "connection changed action: ")
                handleConnectionChangedAction(intent)
            }

            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                Log.d("ahi3646", "handleDiscoveryChangedAction: ")
                networkEventHandler(WiFiNetworkEvent.DiscoveryChanged)
                //handleConnectionChangedAction(intent)
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.d("ahi3646", "onReceive: peers list have changed ")
                wifiP2pManager?.requestPeers(channel, peerListListener)
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                networkEventHandler(WiFiNetworkEvent.ThisDeviceChanged)
            }

            else -> {
                Log.d("ahi3646", "onReceive: Unhandled intent action: $action")
            }
        }
    }

    private fun handleStateChangedAction(intent: Intent) {
        when (val p2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, 0)) {
            WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                Log.d("ahi3646", "handleStateChangedAction: WiFi Direct: Enabled")
                networkEventHandler(WiFiNetworkEvent.WifiStateChanged(isWifiOn = true))
            }

            WifiP2pManager.WIFI_P2P_STATE_DISABLED -> {
                Log.d("ahi3646", "handleStateChangedAction: WiFi Direct: Disabled")
                networkEventHandler(WiFiNetworkEvent.WifiStateChanged(isWifiOn = false))
            }

            else -> {
                Log.d("ahi3646", "handleStateChangedAction: Unknown Wifi p2p state: $p2pState")
            }
        }
    }

    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
        // String from WifiP2pInfo struct
        val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress.orEmpty()
        Log.d("ahi3646", "groupOwnerAddress: $groupOwnerAddress ")
        networkEventHandler(WiFiNetworkEvent.UpdateGroupOwnerAddress(groupOwnerAddress))

        // After the group negotiation, we can determine the group owner
        // (server).
        Log.d("ahi3646", "groupFormed : ${info.groupFormed}  isGroupOwner : ${info.isGroupOwner} ")
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
            Log.d("ahi3646", "connected as a host")
            networkEventHandler(WiFiNetworkEvent.ConnectedAsWhat(isOwner = OwnerStatusState.Owner))
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
            Log.d("ahi3646", "connected as a client")
            networkEventHandler(WiFiNetworkEvent.ConnectedAsWhat(isOwner = OwnerStatusState.Client))
        }
    }

}
