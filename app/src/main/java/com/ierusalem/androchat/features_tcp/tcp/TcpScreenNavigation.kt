package com.ierusalem.androchat.features_tcp.tcp

sealed interface TcpScreenNavigation {
    data object OnNavIconClick : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
    data object OnConnectToServerClick : TcpScreenNavigation
    data object OnCloseServerClick : TcpScreenNavigation
    data object OnDisconnectServerClick : TcpScreenNavigation
    data class OnCreateServerClick(val hotspotName:String, val hotspotPassword:String, val portNumber: Int) : TcpScreenNavigation
}