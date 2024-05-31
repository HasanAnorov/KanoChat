package com.ierusalem.androchat.features_tcp.tcp

import android.net.wifi.p2p.WifiP2pDevice
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.tcp.domain.OwnerStatusState
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenErrors

sealed interface TcpScreenNavigation {
    data object OnNavIconClick : TcpScreenNavigation
    data class WifiDisabledCase(val status : OwnerStatusState ) : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
    data object OnDiscoverWifiClick : TcpScreenNavigation
    data object OnDiscoverHotspotClick : TcpScreenNavigation
    data class OnCreateServerClick(val portNumber: Int) : TcpScreenNavigation
    data class SendClientMessage(val message: Message): TcpScreenNavigation
    data class SendHostMessage(val message: Message): TcpScreenNavigation
    data class OnConnectToWifiClick(val wifiP2pDevice: WifiP2pDevice) : TcpScreenNavigation
    data class OnErrorsOccurred(val tcpScreenErrors: TcpScreenErrors): TcpScreenNavigation
    data class OnConnectToServerClick(val serverIpAddress:String, val portNumber: Int) : TcpScreenNavigation
}