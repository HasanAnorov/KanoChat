package com.ierusalem.androchat.features_tcp.tcp

import android.net.wifi.p2p.WifiP2pDevice
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenErrors

sealed interface TcpScreenNavigation {
    data object OnNavIconClick : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
    data class OnConnectToServerClick(val serverIpAddress:String, val portNumber: Int) : TcpScreenNavigation
    data class OnCreateServerClick(val portNumber: Int) : TcpScreenNavigation
    data object OnDiscoverWifiClick : TcpScreenNavigation
    data class OnConnectToWifiClick(val wifiP2pDevice: WifiP2pDevice) : TcpScreenNavigation
    data class OnErrorsOccurred(val tcpScreenErrors: TcpScreenErrors): TcpScreenNavigation
}