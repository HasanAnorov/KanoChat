package com.ierusalem.androchat.features_local.tcp.presentation

import com.ierusalem.androchat.features_local.tcp.domain.state.TcpScreenErrors
import com.ierusalem.androchat.features_local.tcp.domain.model.ChatMessage

interface TcpScreenNavigation {

    data object OnNavIconClick : TcpScreenNavigation
    data object OnSettingsClick : TcpScreenNavigation
    data object WifiEnableRequest : TcpScreenNavigation
    data object ShowFileChooserClick : TcpScreenNavigation
    data object RequestRecordAudioPermission : TcpScreenNavigation
    data object OnChattingUserClicked : TcpScreenNavigation

    data class OnFileItemClick(val message: ChatMessage.FileMessage) : TcpScreenNavigation
    data class OnErrorsOccurred(val tcpScreenErrors: TcpScreenErrors) : TcpScreenNavigation
    data class OnContactItemClick(val message: ChatMessage.ContactMessage) : TcpScreenNavigation

}