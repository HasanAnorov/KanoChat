package com.ierusalem.androchat.ui.tcp

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.server.broadcast.BroadcastNetworkStatus

@Composable
@CheckResult
fun rememberServerSSID(group: BroadcastNetworkStatus.GroupInfo): String {
  val context = LocalContext.current
  return remember(
      context,
      group,
  ) {
    when (group) {
      is BroadcastNetworkStatus.GroupInfo.Connected -> group.ssid
      is BroadcastNetworkStatus.GroupInfo.Empty -> context.getString(R.string.ssid_no_name)
      is BroadcastNetworkStatus.GroupInfo.Error -> context.getString(R.string.unknown_error)
      is BroadcastNetworkStatus.GroupInfo.Unchanged -> {
        throw IllegalStateException(
            "GroupInfo.Unchanged should never escape the server-module internals.")
      }
    }
  }
}

@Composable
@CheckResult
fun rememberServerRawPassword(group: BroadcastNetworkStatus.GroupInfo): String {
  return remember(
      group,
  ) {
    when (group) {
      is BroadcastNetworkStatus.GroupInfo.Connected -> group.password
      is BroadcastNetworkStatus.GroupInfo.Empty -> ""
      is BroadcastNetworkStatus.GroupInfo.Error -> ""
      is BroadcastNetworkStatus.GroupInfo.Unchanged -> {
        throw IllegalStateException(
            "GroupInfo.Unchanged should never escape the server-module internals.")
      }
    }
  }
}

@Composable
@CheckResult
fun rememberServerPassword(
    group: BroadcastNetworkStatus.GroupInfo,
    isPasswordVisible: Boolean,
): String {
  val context = LocalContext.current
  return remember(
      context,
      group,
      isPasswordVisible,
  ) {
    when (group) {
      is BroadcastNetworkStatus.GroupInfo.Connected -> {
        val rawPassword = group.password
        // If hidden password, map each char to the password star
        return@remember if (isPasswordVisible) {
          rawPassword
        } else {
          rawPassword.map { '\u2022' }.joinToString("")
        }
      }
      is BroadcastNetworkStatus.GroupInfo.Empty -> {
        context.getString(R.string.passwd_none)
      }
      is BroadcastNetworkStatus.GroupInfo.Error -> {
        context.getString(R.string.unknown_error)
      }
      is BroadcastNetworkStatus.GroupInfo.Unchanged -> {
        throw IllegalStateException(
            "GroupInfo.Unchanged should never escape the server-module internals.")
      }
    }
  }
}

@Composable
@CheckResult
fun rememberServerHostname(connection: BroadcastNetworkStatus.ConnectionInfo): String {
  val context = LocalContext.current
  return remember(
      connection,
      context,
  ) {
    when (connection) {
      is BroadcastNetworkStatus.ConnectionInfo.Connected -> connection.hostName
      is BroadcastNetworkStatus.ConnectionInfo.Empty -> context.getString(R.string.server_no_host)
      is BroadcastNetworkStatus.ConnectionInfo.Error -> context.getString(R.string.unknown_error)
      is BroadcastNetworkStatus.ConnectionInfo.Unchanged -> {
        throw IllegalStateException(
            "ConnectionInfo.Unchanged should never escape the server-module internals.")
      }
    }
  }
}
