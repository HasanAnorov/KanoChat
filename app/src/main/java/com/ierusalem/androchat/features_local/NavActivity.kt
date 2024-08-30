package com.ierusalem.androchat.features_local

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.databinding.ActivityNavBinding
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_local.tcp.data.server.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

/**
 * NavActivity
 *
 * @author A.H.I "andro" on 28/08/2024
 */

@Suppress("unused")
@AndroidEntryPoint
class NavActivity : AppCompatActivity() {

    private val viewModel: TcpViewModel by viewModels()

    @Inject
    lateinit var wifiP2PManager: WifiP2pManager

    @Inject
    lateinit var channel: WifiP2pManager.Channel

    //todo delegate this to viewmodel
    private lateinit var permissionGuard: PermissionGuardImpl

    //gson to convert message object to string
    private lateinit var gson: Gson

    //resource directory
    private lateinit var resourceDirectory: File

    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private val intentFilter = IntentFilter()

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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }


        intentFilter.apply {
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        }

        permissionGuard = PermissionGuardImpl(this)
        gson = Gson()
        resourceDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}"
        )!!

        setContentView(
            ComposeView(this@NavActivity).apply {
                consumeWindowInsets = false
                setContent {
                    AndroChatTheme {
                        AndroidViewBinding(ActivityNavBinding::inflate)
                    }
                }
            }
        )
    }

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
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        return navHostFragment.navController
    }

}
