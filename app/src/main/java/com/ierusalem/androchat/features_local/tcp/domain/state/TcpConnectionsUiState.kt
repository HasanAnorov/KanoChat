package com.ierusalem.androchat.features_local.tcp.domain.state

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ierusalem.androchat.R

enum class GeneralConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.not_running),
    ConnectedAsHost(R.string.connected_as_host),
    ConnectedAsClient(R.string.connected_as_client)
}

enum class ClientConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.connect),
    Connecting(R.string.connecting_to_server),
    Connected(R.string.connected_to_a_server),
    Failure(R.string.couldn_t_connect_to_server);

    @Composable
    fun getIconColor(): Color {
        return when (this) {
            Idle -> MaterialTheme.colorScheme.onErrorContainer
            Connecting -> Color(0xFF35C47C)
            Connected -> Color(0xFF35C47C)
            Failure -> Color.Red
        }
    }

}

enum class HostConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.create_a_server),
    Creating(R.string.creating_a_server),
    Created(R.string.server_created_waiting_for_clients),
    Failure(R.string.couldn_t_create_a_server);

    @Composable
    fun getIconColor(): Color {
        return when (this) {
            Idle -> MaterialTheme.colorScheme.onErrorContainer
            Creating -> Color(0xFF35C47C)
            Created -> Color(0xFF35C47C)
            Failure -> Color.Red
        }
    }

}
