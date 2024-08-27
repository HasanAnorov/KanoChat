package com.ierusalem.androchat.features_local.tcp.domain.state

import androidx.annotation.StringRes
import com.ierusalem.androchat.R

//todo add comment and remove role from and remove role from general connection status
enum class GeneralConnectionStatus(@StringRes val status: Int, @StringRes val role: Int) {
    Idle(R.string.not_running, R.string.not_defined),
    ConnectedAsHost(R.string.connected_as_host, R.string.owner),
    ConnectedAsClient(R.string.connected_as_client, R.string.client)
}

enum class ClientConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.connect),
    Connecting(R.string.connecting_to_server),
    Connected(R.string.connected_to_a_server),
    Failure(R.string.couldn_t_connect_to_server)
}

enum class HostConnectionStatus(@StringRes val status: Int) {
    Idle(R.string.create_a_server),
    Creating(R.string.creating_a_server),
    Created(R.string.server_created_waiting_for_clients),
    Failure(R.string.couldn_t_create_a_server)
}
