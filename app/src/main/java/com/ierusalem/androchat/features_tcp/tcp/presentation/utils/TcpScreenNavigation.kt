package com.ierusalem.androchat.features_tcp.tcp.presentation.utils

import android.net.wifi.p2p.WifiP2pDevice
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenErrors

sealed interface TcpScreenNavigation {
    data object OnNavIconClick : TcpScreenNavigation
    data object WifiDisabledCase : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
    data object OnDiscoverP2PClick : TcpScreenNavigation
    data object OnStartHotspotNetworking : TcpScreenNavigation
    data object OnStopHotspotNetworking : TcpScreenNavigation
    data object OnStopP2PDiscovery : TcpScreenNavigation
    data class OnCreateServerClick(val portNumber: Int) : TcpScreenNavigation
    data class SendClientMessage(val message: Message) : TcpScreenNavigation
    data class SendHostMessage(val message: Message) : TcpScreenNavigation
    data class OnConnectToWifiClick(val wifiP2pDevice: WifiP2pDevice) : TcpScreenNavigation
    data class OnErrorsOccurred(val tcpScreenErrors: TcpScreenErrors) : TcpScreenNavigation
    data class OnConnectToServerClick(val serverIpAddress: String, val portNumber: Int) :
        TcpScreenNavigation

    data object OnReadContactsRequest: TcpScreenNavigation
    data object ShowFileChooserClick: TcpScreenNavigation

    data class OnFileItemClick(val message: Message.FileMessage): TcpScreenNavigation
}