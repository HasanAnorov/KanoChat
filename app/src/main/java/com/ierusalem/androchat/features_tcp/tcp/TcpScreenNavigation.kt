package com.ierusalem.androchat.features_tcp.tcp

sealed interface TcpScreenNavigation {
    data object OnNavIconClick : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
}