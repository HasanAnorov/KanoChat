package com.ierusalem.androchat.features_tcp.tcp

import android.net.wifi.p2p.WifiP2pDevice

sealed interface TcpScreenEvents {
    data object OnNavIconClick : TcpScreenEvents
    data object OnSettingIconClick : TcpScreenEvents
    data object OpenHotspotClick : TcpScreenEvents
    data class OnWifiStateChanged(val isWifiEnabled: Boolean) : TcpScreenEvents
    data object ConnectToServerClick : TcpScreenEvents
    data object CreateWifiClick : TcpScreenEvents
    data object DiscoverWifiClick : TcpScreenEvents
    data class OnConnectToWifiClick(val wifiDevice: WifiP2pDevice) : TcpScreenEvents
}