package com.ierusalem.androchat.features_local.tcp.domain.state

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ierusalem.androchat.R

enum class TcpScreenErrors(@StringRes val errorMessage: Int) {
    WifiNotEnabled(R.string.wifi_should_be_enabled_to_perform_this_action),
    InvalidPortNumber(R.string.try_to_use_another_port_number_current_port_is_already_in_use_or_invalid),
    InvalidWiFiServerIpAddress(R.string.current_connected_wifi_server_ip_address_is_not_a_valid),
    FailedToConnectToWifiDevice(R.string.couldn_t_connect_to_chosen_wifi_device),
}

enum class TcpScreenDialogErrors(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @DrawableRes val icon: Int
) {
    LocalOnlyHotspotNotSupported(
        R.string.android_version_8_required,
        R.string.android_version_8_required_for_local_only_hotspot,
        R.drawable.info
    ),
    ServerCreationWithoutNetworking(
        R.string.please_connect_to_network,
        R.string.you_should_be_on_network_to_connect_to_server,
        R.drawable.wifi_off
    ),
    EOException(
        R.string.network_error_occurred,
        R.string.your_network_connection_was_interrupted_check_your_connection_and_try_again,
        R.drawable.wifi_off
    ),
    IOException(
        R.string.network_error_occurred,
        R.string.your_network_connection_was_interrupted_check_your_connection_and_try_again,
        R.drawable.wifi_off
    ),
    UTFDataFormatException(
        R.string.network_error_occurred,
        R.string.incoming_messages_are_not_in_utf_8_format_the_data_do_not_represent_a_valid_modified_utf_8_encoding_of_a_string,
        R.drawable.error_prompt
    ),
    UnknownHostException(
        R.string.invalid_host_ip_address,
        R.string.the_ip_address_of_the_host_could_not_be_determined,
        R.drawable.info
    ),
    EstablishConnectionToSendMessage(
        R.string.no_one_to_chat,
        R.string.could_not_establish_connection_with_your_partner_please_try_to_reconnect_and_try_again,
        R.drawable.info
    ),
    OtherNetworkingIsRunning(
        R.string.other_networking_is_running,
        R.string.other_networking_is_running_cancel_it_to_create_peer_networking,
        R.drawable.info
    ),
    PeerNotConnected(
        R.string.no_peer_to_chat,
        R.string.there_is_no_online_peers_to_chat,
        R.drawable.info
    ),
    SecurityException(
        R.string.security_exception_occurred,
        R.string.security_exception_occurred_definition,
        R.drawable.info
    ),
    IllegalArgumentException(
        R.string.illegal_argument_exception_occurred,
        R.string.illegal_argument_exception_occurred_definition,
        R.drawable.info
    ),
    UnknownException(
        R.string.unknown_exception_occurred,
        R.string.unknown_exception_occurred_definition,
        R.drawable.info
    )
}