package com.ierusalem.androchat.features_tcp.tcp.presentation.utils

import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features_tcp.tcp.domain.state.TcpScreenDialogErrors

sealed interface TcpScreenEvents {
    data object OnNavIconClick : TcpScreenEvents
    data object OnSettingIconClick : TcpScreenEvents
    data object CreateServerClick : TcpScreenEvents
    data object ConnectToServerClick : TcpScreenEvents
    data object DiscoverP2PClick : TcpScreenEvents
    data object DiscoverHotSpotClick : TcpScreenEvents
    data object DiscoverLocalOnlyHotSpotClick : TcpScreenEvents
    data class OnPortNumberChanged(val portNumber: String) : TcpScreenEvents
    data class OnHotspotNameChanged(val hotspotName: String) : TcpScreenEvents
    data class OnConnectToWifiClick(val wifiDevice: WifiP2pDevice) : TcpScreenEvents
    data class SendMessageRequest(val message:String): TcpScreenEvents
    data class OnDialogErrorOccurred(val error: TcpScreenDialogErrors?): TcpScreenEvents

    //local conversation
    data class HandlePickingMultipleMedia(val medias:List<Uri>):TcpScreenEvents
    data class ReadContactPermissionChanged(val isGranted: Boolean): TcpScreenEvents
    data object ReadContactsRequest: TcpScreenEvents
    data object ReadContacts: TcpScreenEvents
    data object ShowFileChooserClick: TcpScreenEvents
    data class UpdateBottomSheetState(val shouldBeShown:Boolean): TcpScreenEvents

    data class OnFileItemClick(val message: Message.FileMessage): TcpScreenEvents
    data class OnContactItemClick(val message: Message.ContactMessage): TcpScreenEvents
}