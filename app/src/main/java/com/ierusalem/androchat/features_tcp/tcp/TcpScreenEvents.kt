package com.ierusalem.androchat.features_tcp.tcp

import android.net.wifi.p2p.WifiP2pDevice

sealed interface TcpScreenEvents {
    data object OnNavIconClick : TcpScreenEvents
    data object OnSettingIconClick : TcpScreenEvents
    data object CreateServerClick : TcpScreenEvents
    data object ConnectToServerClick : TcpScreenEvents
    data object DiscoverWifiClick : TcpScreenEvents
    data class OnPortNumberChanged(val portNumber: String) : TcpScreenEvents
    data class OnConnectToWifiClick(val wifiDevice: WifiP2pDevice) : TcpScreenEvents
    data class SendMessage(val message:String): TcpScreenEvents
}