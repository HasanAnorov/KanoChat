package com.ierusalem.androchat.features_tcp.tcp

import android.net.wifi.p2p.WifiP2pDevice
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.tcp.domain.ClientConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.HostConnectionStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpScreenDialogErrors

sealed interface TcpScreenEvents {
    data object OnNavIconClick : TcpScreenEvents
    data object OnSettingIconClick : TcpScreenEvents
    data object CreateServerClick : TcpScreenEvents
    data object ConnectToServerClick : TcpScreenEvents
    data object DiscoverWifiClick : TcpScreenEvents
    data object DiscoverHotSpotClick : TcpScreenEvents
    data class UpdateClientStatus(val status: ClientConnectionStatus):TcpScreenEvents
    data class UpdateServerStatus(val status: HostConnectionStatus):TcpScreenEvents
    data class OnPortNumberChanged(val portNumber: String) : TcpScreenEvents
    data class OnConnectToWifiClick(val wifiDevice: WifiP2pDevice) : TcpScreenEvents
    data class SendMessageRequest(val message:String): TcpScreenEvents
    data class InsertMessage(val message:Message): TcpScreenEvents
    data class OnDialogErrorOccurred(val error:TcpScreenDialogErrors?): TcpScreenEvents
}