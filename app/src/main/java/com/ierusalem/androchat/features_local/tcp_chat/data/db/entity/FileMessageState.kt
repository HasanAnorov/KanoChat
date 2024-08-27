package com.ierusalem.androchat.features_local.tcp_chat.data.db.entity

sealed interface FileMessageState {
    data class Loading(val percentage: Int) : FileMessageState
    data object Success : FileMessageState
    data object Failure : FileMessageState
}