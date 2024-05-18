package com.ierusalem.androchat.features_tcp.tcp.tcp

import com.ierusalem.androchat.R
import com.ierusalem.androchat.utils.UiText

enum class TcpView(val displayName: UiText) {
    STATUS(UiText.StringResource(R.string.hotspot)),
    CONNECTIONS(UiText.StringResource(R.string.connections)),
    INFO(UiText.StringResource(R.string.help))
}