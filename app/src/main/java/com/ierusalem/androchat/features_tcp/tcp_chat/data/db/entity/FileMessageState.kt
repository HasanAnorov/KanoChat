package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity

//todo - finish proper file handling ...
sealed interface FileMessageState {
    data class Loading(val percentage: Int) : FileMessageState
    data object Success : FileMessageState
    data object Failure : FileMessageState
}