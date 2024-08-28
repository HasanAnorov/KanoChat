package com.ierusalem.androchat.features_local.tcp.domain.state

import android.net.wifi.p2p.WifiP2pDevice
import androidx.paging.PagingData
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.app.AppBroadcastFrequency
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.Resource
import com.ierusalem.androchat.core.utils.UiText
import com.ierusalem.androchat.core.utils.generateRandomPassword
import com.ierusalem.androchat.core.utils.isValidHotspotName
import com.ierusalem.androchat.core.utils.isValidHotspotPassword
import com.ierusalem.androchat.core.utils.isValidIpAddress
import com.ierusalem.androchat.core.utils.isValidPortNumber
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp.domain.InitialChatModel
import com.ierusalem.androchat.features_local.tcp_conversation.data.db.entity.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class TcpScreenUiState(

    val userUniqueName:String = Constants.UNKNOWN_USER,
    val peerUniqueName: String = Constants.UNKNOWN_PEER,

    val contacts: Resource<List<ContactItem>> = Resource.Loading(),
    val isReadContactsGranted: Boolean = false,

    val showBottomSheet: Boolean = false,

    //general state
    val isWifiOn: Boolean = false,
    val peerUserUniqueId: String = "",

    val hotspotName: String = Constants.UNKNOWN_HOTSPOT_NAME,
    val isValidHotSpotName: Boolean = isValidHotspotName(hotspotName),
    val hotspotPassword: String = generateRandomPassword(length = 8),
    val isValidHotSpotPassword: Boolean = isValidHotspotPassword(hotspotPassword),

    val portNumber: String = Constants.DEFAULT_PORT_NUMBER,
    val isValidPortNumber: Boolean = isValidPortNumber(portNumber),
    val groupOwnerAddress: String = "Not connected",
    val isValidGroupOwnerAddress: Boolean = isValidIpAddress(groupOwnerAddress),

    val connectedServerAddress : UiText = UiText.StringResource(R.string.not_connected),

    //general networking state
    val generalNetworkingStatus: GeneralNetworkingStatus = GeneralNetworkingStatus.Idle,
    val localOnlyHotspotNetworkingStatus: LocalOnlyHotspotStatus = LocalOnlyHotspotStatus.Idle,
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
//    val messages: List<ChatMessage> = emptyList(),
    val messages: Flow<PagingData<ChatMessage>> = flowOf(),

    //contacts
    val contactsList:Resource<List<ChattingUserEntity>> = Resource.Loading(),

    //but Erkin aka said, chat should be between only two people
    //todo - maybe you should use connectedWifiNetworks.size here !
    val connectionsCount: Int = 0,

    //this wifi address used for connecting to server by clients, don't use this for host side
    val connectedWifiAddress: String = "Not created",
    val isValidConnectedWifiAddress: Boolean = isValidIpAddress(connectedWifiAddress),

    //error handling with dialogs
    val hasDialogErrorOccurred: TcpScreenDialogErrors? = null,

    //to disable view pager's horizontal scrolling
    val isRecording: Boolean = false,

    val networkBand: AppBroadcastFrequency = AppBroadcastFrequency.FREQUENCY_2_4_GHZ,

    //defines current chatting user unique id, used in conversation screen only
    val currentChattingUser: InitialChatModel? = null
)
