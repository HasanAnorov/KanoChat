package com.ierusalem.androchat.features_tcp.tcp.domain.state

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ierusalem.androchat.R

enum class TcpCloseDialogReason(@StringRes val reason: Int, @StringRes val description: Int) {

    //This error case will be removed when "saving existing messages" feature will be implemented
    ExistingMessages(
        R.string.message_are_not_saved,
        R.string.you_have_existing_messages_with_your_partner_and_if_you_close_the_this_window_messages_will_not_be_saved
    ),

    ExistingConnection(
        R.string.the_connection_will_not_be_saved,
        R.string.you_have_established_connection_with_your_partner_if_you_close_this_window_the_connection_will_not_be_saved
    )
}

enum class TcpScreenErrors(@StringRes val errorMessage: Int) {
    WifiNotEnabled(R.string.wifi_should_be_enabled_to_perform_this_action),
    AlreadyDiscoveringWifi(R.string.already_discovering_wifi_networks),
    InvalidPortNumber(R.string.try_to_use_another_port_number_current_port_is_already_in_use_or_invalid),
    InvalidHostAddress(R.string.try_to_reconnect_to_the_server_again_current_address_is_invalid),
    InvalidWiFiServerIpAddress(R.string.current_connected_wifi_server_ip_address_is_not_a_valid),
    FailedToConnectToWifiDevice(R.string.couldn_t_connect_to_chosen_wifi_device),
}

enum class TcpScreenDialogErrors(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @DrawableRes val icon: Int
) {
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
    YouAreNotOwner(
        R.string.you_are_not_the_owner,
        R.string.you_connected_as_client_thus_you_can_t_create_a_server,
        R.drawable.info
    ),
    YouAreNotClient(
        R.string.you_are_not_the_client,
        R.string.you_connected_as_a_host_for_this_server_thus_you_can_t_be_a_client,
        R.drawable.info
    ),
    ServerCreationOrConnectionWithoutWifiConnection(
        R.string.no_wifi_connection,
        R.string.you_need_to_connect_to_a_wifi_network_to_create_or_connect_to_a_server,
        R.drawable.info
    ),
    EstablishConnectionToSendMessage(
        R.string.no_one_to_chat,
        R.string.could_not_establish_connection_with_your_partner_please_try_to_reconnect_and_try_again,
        R.drawable.info
    ),
    AlreadyP2PNetworkingRunning(
        R.string.peer_networking_is_running,
        R.string.peer_networking_is_running_cancel_it_to_creat_group_networking,
        R.drawable.info
    ),
    AlreadyHotspotNetworkingRunning(
        R.string.group_networking_is_running,
        R.string.group_networking_is_running_cancel_it_to_create_peer_networking,
        R.drawable.info
    ),
    AndroidVersion10RequiredForGroupNetworking(
        R.string.your_device_does_not_support_group_networking,
        R.string.at_least_android_10_is_required_for_group_networking,
        R.drawable.info
    ),
    PeerNotConnected(
        R.string.no_peer_to_chat,
        R.string.there_is_no_online_peers_to_chat,
        R.drawable.info
    ),
}