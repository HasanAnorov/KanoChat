package com.ierusalem.androchat.features_local.tcp.presentation.utils

import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import com.ierusalem.androchat.features_local.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenErrors
import com.ierusalem.androchat.features_local.tcp_chat.data.db.entity.ChatMessageEntity

sealed interface TcpScreenNavigation {
    data object OnNavIconClick : TcpScreenNavigation
    data object WifiDisabledCase : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
    data object OnDiscoverP2PClick : TcpScreenNavigation
    data object OnStartHotspotNetworking : TcpScreenNavigation
    data object OnStopHotspotNetworking : TcpScreenNavigation
    data object OnStopP2PDiscovery : TcpScreenNavigation
    data class OnCreateServerClick(val portNumber: Int) : TcpScreenNavigation
    data class SendClientMessage(val message: ChatMessageEntity) : TcpScreenNavigation
    data class SendHostMessage(val message: ChatMessageEntity) : TcpScreenNavigation
    data class OnConnectToWifiClick(val wifiP2pDevice: WifiP2pDevice) : TcpScreenNavigation
    data class OnErrorsOccurred(val tcpScreenErrors: TcpScreenErrors) : TcpScreenNavigation
    data class OnConnectToServerClick(val serverIpAddress: String, val portNumber: Int) :
        TcpScreenNavigation

    data object WifiEnableRequest: TcpScreenNavigation

    data object ShowFileChooserClick: TcpScreenNavigation

    data object RequestReadContactsPermission: TcpScreenNavigation
    data object RequestRecordAudioPermission: TcpScreenNavigation

    data class OnFileItemClick(val message: ChatMessage.FileMessage): TcpScreenNavigation
    data class OnContactItemClick(val message: ChatMessage.ContactMessage): TcpScreenNavigation
    data class HandlePickingMultipleMedia(val medias: List<Uri>):TcpScreenNavigation

    data class OnChattingUserClicked(val userUniqueId: String): TcpScreenNavigation
}