package com.ierusalem.androchat.features_local

import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.features_local.tcp.data.server.wifidirect.WiFiDirectBroadcastReceiver
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * NavActivity
 *
 * @author A.H.I "andro" on 28/08/2024
 */

@Suppress("unused")
@AndroidEntryPoint
class LocalActivity : AppCompatActivity() {

    private val viewModel: TcpViewModel by viewModels()

    @Inject
    lateinit var dataStorePreferenceRepository: DataStorePreferenceRepository

    @Inject
    lateinit var wifiP2PManager: WifiP2pManager

    @Inject
    lateinit var channel: WifiP2pManager.Channel

    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private val intentFilter = IntentFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        //as long as you are saving isOnline in DB, initial value should be false for all users
        viewModel.updateAllUsersOnlineStatus(false)

        intentFilter.apply {
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        }

        // Setup the NavHostFragment and set the graph before setting content view
        val navHostFragment = NavHostFragment.create(R.navigation.nav_graph_local)
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, navHostFragment)
            .setPrimaryNavigationFragment(navHostFragment)
            .commitNow()

        // Set up navigation graph and start destination
        setupNavigationGraph()

        // Ensure your XML layout contains a NavHostFragment with ID nav_host_fragment
        setContentView(R.layout.activity_local)

    }

    private fun setupNavigationGraph() {
        val navController = findNavController()
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_graph_local)

        // Set the start destination synchronously based on login state
        val startDestination = if (runBlocking { hasUserLoggedIn() }) {
            R.id.tcpFragment
        } else {
            R.id.loginFragment
        }

        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }

    private suspend fun hasUserLoggedIn(): Boolean {
        return withContext(Dispatchers.IO) {
            dataStorePreferenceRepository.hasUserLoggedIn()
        }
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
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

}
