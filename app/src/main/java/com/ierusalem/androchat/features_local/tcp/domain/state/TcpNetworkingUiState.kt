package com.ierusalem.androchat.features_local.tcp.domain.state

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralNetworkingStatus.HotspotDiscovery
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralNetworkingStatus.Idle
import com.ierusalem.androchat.features_local.tcp.domain.state.GeneralNetworkingStatus.P2PDiscovery
import com.ierusalem.androchat.features_local.tcp.domain.state.HotspotNetworkingStatus.HotspotRunning
import com.ierusalem.androchat.features_local.tcp.domain.state.HotspotNetworkingStatus.LaunchingHotspot
import com.ierusalem.androchat.features_local.tcp.domain.state.P2PNetworkingStatus.Discovering
import com.ierusalem.androchat.features_local.tcp.domain.state.P2PNetworkingStatus.Failure

/**
 * Indicates the state of discovering wifi networks
 * @property Idle wifi discovery has not started yet
 * @property HotspotDiscovery wifi discovery started creates hotspot for connection
 * @property P2PDiscovery wifi discovery started creates p2p connection
 * @property LocalOnlyHotspot wifi discovery started creates local only hotspot
 */
enum class GeneralNetworkingStatus(@StringRes val status: Int) {
    Idle(R.string.not_running),
    HotspotDiscovery(R.string.group_networking_is_running),
    P2PDiscovery(R.string.peer_networking_is_running),
    LocalOnlyHotspot(R.string.local_only_group_networking_is_running)
}

/**
 * Indicates the state of P2P networking
 * @property Idle peer networking has not started yet
 * @property Discovering peer networking has started and continues to discover wifi networks
 * @property Failure peer networking failed to start
 */
enum class P2PNetworkingStatus(@StringRes val res: Int, @DrawableRes val icon: Int) {
    Idle(R.string.peer_networking, R.drawable.wifi),
    Discovering(R.string.stop_peer_networking, R.drawable.wifi),
    Failure(R.string.discovering_not_started, R.drawable.error_prompt)
}

/**
 * Indicates the state of hotspot networking
 * @property Idle peet networking has not started yet
 * @property LaunchingHotspot hotspot has starting its job
 * @property HotspotRunning hotspot has fully started and is running
 * @property Failure hotspot failed to start
 */
enum class HotspotNetworkingStatus(@StringRes val res: Int, @DrawableRes val icon: Int) {
    Idle(R.string.group_networking, R.drawable.wifi),
    LaunchingHotspot(R.string.launching_hotspot, R.drawable.wifi),
    HotspotRunning(R.string.stop_group_networking, R.drawable.wifi),
    Failure(R.string.couldn_t_launch_hotspot, R.drawable.error_prompt)
}

/**
 * Indicates the state of local-only hotspot networking
 * @property Idle networking has not started yet
 * @property LaunchingLocalOnlyHotspot local-only hotspot has starting its job
 * @property LocalOnlyHotspotRunning local-only hotspot has fully started and is running
 * @property Failure local-only hotspot failed to start
 */
enum class LocalOnlyHotspotStatus(@StringRes val res: Int, @DrawableRes val icon: Int){
    Idle(R.string.start_local_only_hotspot, R.drawable.wifi),
    LaunchingLocalOnlyHotspot(R.string.launching_hotspot, R.drawable.wifi),
    LocalOnlyHotspotRunning(R.string.stop_local_only_hotspot, R.drawable.wifi),
    Failure(R.string.couldn_t_launch_hotspot, R.drawable.error_prompt)
}
