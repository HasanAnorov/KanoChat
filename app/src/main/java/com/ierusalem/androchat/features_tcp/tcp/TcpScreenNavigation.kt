package com.ierusalem.androchat.features_tcp.tcp

import android.net.wifi.p2p.WifiP2pDevice

sealed interface TcpScreenNavigation {
    data object OnNavIconClick : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
    data object OnCreateWiFiClick : TcpScreenNavigation
    data object OnConnectToServerClick : TcpScreenNavigation
    data object OnCloseServerClick : TcpScreenNavigation
    data object OnDisconnectServerClick : TcpScreenNavigation
    data object OnDiscoverWifiClick : TcpScreenNavigation
    data class OnCreateServerClick(val hotspotName:String, val hotspotPassword:String, val portNumber: Int) : TcpScreenNavigation
    data class OnConnectToWifiClick(val wifiP2pDevice: WifiP2pDevice) : TcpScreenNavigation
}