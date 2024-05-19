package com.ierusalem.androchat.features_tcp.tcp

sealed interface TcpScreenEvents {
    data object OnNavIconClick : TcpScreenEvents
    data object OnSettingIconClick : TcpScreenEvents
    data object OpenHotspotClick : TcpScreenEvents
    data object ConnectToServerClick : TcpScreenEvents
}