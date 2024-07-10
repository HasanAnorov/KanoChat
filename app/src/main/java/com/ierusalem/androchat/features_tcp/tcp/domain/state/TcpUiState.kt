package com.ierusalem.androchat.features_tcp.tcp.domain.state

import android.net.wifi.p2p.WifiP2pDevice
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.constants.Constants
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.core.utils.isValidHotspotName
import com.ierusalem.androchat.core.utils.isValidIpAddress
import com.ierusalem.androchat.core.utils.isValidPortNumber
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import java.util.UUID

data class TcpScreenUiState(

    val contacts:Resource<List<ContactItem>> = Resource.Loading(),
    val isReadContactsGranted: Boolean = false,
    val shouldShowPermissionDialog: Boolean = false,
    val showBottomSheet: Boolean = false,

    //general state
    val isWifiOn: Boolean = false,
    val authorMe: String = Constants.UNKNOWN_USER,
    val hotspotName: String = Constants.UNKNOWN_HOTSPOT_NAME,
    val isValidHotSpotName: Boolean = isValidHotspotName(hotspotName),

    val portNumber: String = Constants.DEFAULT_PORT_NUMBER,
    val isValidPortNumber: Boolean = isValidPortNumber(portNumber),
    val groupOwnerAddress: String = "Not created",
    val isValidGroupOwnerAddress: Boolean = isValidIpAddress(groupOwnerAddress),

    val localOnlyHotspotStatus:LocalOnlyHotspotStatus = LocalOnlyHotspotStatus.Idle,

    //general networking state
    val generalNetworkingStatus: GeneralNetworkingStatus = GeneralNetworkingStatus.Idle,
    val hotspotNetworkingStatus: HotspotNetworkingStatus = HotspotNetworkingStatus.Idle,
    val p2pNetworkingStatus: P2PNetworkingStatus = P2PNetworkingStatus.Idle,

    //general connections state - todo optimize this
    val generalConnectionStatus: GeneralConnectionStatus = GeneralConnectionStatus.Idle,
    val hostConnectionStatus: HostConnectionStatus = HostConnectionStatus.Idle,
    val clientConnectionStatus: ClientConnectionStatus = ClientConnectionStatus.Idle,

    //wifi peers list and connected wifi peers list
    val availableWifiNetworks: List<WifiP2pDevice> = emptyList(),
    val connectedWifiNetworks: List<WifiP2pDevice> = emptyList(),

    //chat room
    val messages: List<Message> = emptyList(),
    //but Erkin aka said, chat should be between only two people
    //todo maybe you should use connectedWifiNetworks.size here !!!
    val connectionsCount: Int = 0,

    //this wifi address used for connecting to server by clients, don't use this for host side
    val connectedWifiAddress: String ="Not created",
    val isValidConnectedWifiAddress: Boolean = isValidIpAddress(connectedWifiAddress),

    //error handling
    val hasDialogErrorOccurred: TcpScreenDialogErrors? = null
)

enum class LocalOnlyHotspotStatus(@StringRes val res: Int, @DrawableRes val icon: Int){
    Idle(R.string.start_local_only_hotspot, R.drawable.wifi),
    LaunchingHotspot(R.string.launching_hotspot, R.drawable.wifi),
    HotspotRunning(R.string.stop_local_only_hotspot, R.drawable.wifi),
    Failure(R.string.couldn_t_launch_hotspot, R.drawable.error_prompt)
}

@Immutable
data class ContactItem(
    val contactName:String,
    val phoneNumber:String,
    val isSelected: Boolean,
    val id: String = UUID.randomUUID().toString(),
)



