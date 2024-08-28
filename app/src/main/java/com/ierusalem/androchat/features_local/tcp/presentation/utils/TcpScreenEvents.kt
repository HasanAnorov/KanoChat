package com.ierusalem.androchat.features_local.tcp.presentation.utils

import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import com.ierusalem.androchat.features_local.tcp.domain.InitialChatModel
import com.ierusalem.androchat.features_local.tcp_chat.data.db.entity.ChatMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenDialogErrors

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
    data class OnHotspotPasswordChanged(val hotspotPassword: String) : TcpScreenEvents

    data class OnConnectToWifiClick(val wifiDevice: WifiP2pDevice) : TcpScreenEvents

    data class SendMessageRequest(val message: String) : TcpScreenEvents

    data class OnDialogErrorOccurred(val error: TcpScreenDialogErrors?) : TcpScreenEvents

    data class HandlePickingMultipleMedia(val medias: List<Uri>) : TcpScreenEvents

    data object RequestRecordAudioPermission : TcpScreenEvents
    data object RequestReadContactsPermission : TcpScreenEvents

    data object ReadContacts : TcpScreenEvents
    data object ShowFileChooserClick : TcpScreenEvents

    data class UpdateBottomSheetState(val shouldBeShown: Boolean) : TcpScreenEvents

    data class TcpContactItemClicked(val currentChattingUser: InitialChatModel) : TcpScreenEvents

    data class OnFileItemClick(val message: ChatMessage.FileMessage) : TcpScreenEvents
    data class OnContactItemClick(val message: ChatMessage.ContactMessage) : TcpScreenEvents

    data object OnVoiceRecordStart : TcpScreenEvents
    data object OnVoiceRecordFinished : TcpScreenEvents
    data object OnVoiceRecordCancelled : TcpScreenEvents

    data class OnPlayVoiceMessageClick(val message: ChatMessage.VoiceMessage) : TcpScreenEvents
    data class OnPauseVoiceMessageClick(val message: ChatMessage.VoiceMessage) : TcpScreenEvents
    data class OnStopVoiceMessageClick(val message: ChatMessage.VoiceMessage) : TcpScreenEvents
}