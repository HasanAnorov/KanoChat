package com.ierusalem.androchat.features_tcp.server.broadcast

import android.util.Log
import androidx.annotation.CheckResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.ierusalem.androchat.features_tcp.server.IP_ADDRESS_REGEX
import com.ierusalem.androchat.features_tcp.server.Server
import com.ierusalem.androchat.features_tcp.server.status.RunningStatus
import kotlinx.coroutines.flow.Flow

interface BroadcastNetworkStatus : Server {

  @CheckResult fun onGroupInfoChanged(): Flow<GroupInfo>

  @CheckResult fun onConnectionInfoChanged(): Flow<ConnectionInfo>

  @CheckResult fun getCurrentProxyStatus(): RunningStatus

  @CheckResult fun onProxyStatusChanged(): Flow<RunningStatus>

  @Stable
  @Immutable
  sealed interface GroupInfo {

    data object Unchanged : GroupInfo

    data class Connected
    internal constructor(
        val ssid: String,
        val password: String,
    ) : GroupInfo

    data object Empty : GroupInfo

    data class Error
    internal constructor(
        val error: Throwable,
    ) : GroupInfo

    @CheckResult
    fun update(onUpdate: (Connected) -> Connected): GroupInfo {
      return when (this) {
        is Connected -> onUpdate(this)
        is Empty -> this
        is Error -> this
        is Unchanged -> this
      }
    }
  }

  @Stable
  @Immutable
  sealed interface ConnectionInfo {
    data object Unchanged : ConnectionInfo

    data class Connected
    internal constructor(
        val hostName: String,
    ) : ConnectionInfo {

      private data class NotAnIpAddressException(val hostName: String) :
          IllegalStateException("Not an IP address: $hostName")

      // Is this Connection info from the server an IP address or a DNS hostname?
      // It's almost always an IP address
      val isIpAddress by lazy { IP_ADDRESS_REGEX.matches(hostName) }

      /**
       * Split up an IP address
       *
       * 192.168.49.1 -> [ 192, 168, 49, 1 ]
       */
      private val splitUpIp by lazy {
        ensureIpAddress()
        hostName.split(".")
      }

      /**
       * IP first block
       *
       * 192.168.49.1 -> 192
       */
      private val ipFirstBlock by lazy {
        ensureIpAddress()
        splitUpIp[0]
      }

      /**
       * IP second block
       *
       * 192.168.49.1 -> 168
       */
      private val ipSecondBlock by lazy {
        ensureIpAddress()
        splitUpIp[1]
      }

      /**
       * IP third block
       *
       * 192.168.49.1 -> 49
       */
      private val ipThirdBlock by lazy {
        ensureIpAddress()
        splitUpIp[2]
      }

      /** If this is not an IP address, you've done something wrong */
      private fun ensureIpAddress() {
        if (!isIpAddress) {
          throw NotAnIpAddressException(hostName)
        }
      }

      /**
       * If the server is an IP address, then we should check that this client is also IP
       * addressable.
       *
       * If the client is a hostname client (do we have any of these ever?), then this method does
       * not apply
       */
      @CheckResult
      fun isClientWithinAddressableIpRange(ip: String): Boolean {
        val splitUpAddress = ip.split(".")

        // Needs to be 4 sections
        if (splitUpAddress.size != 4) {
          Log.d("ahi3646","isClientWithinAddressableIpRange: Split up IP address was wrong format: $ip $splitUpAddress")
          return false
        }

        val first = splitUpAddress[0]
        val second = splitUpAddress[1]
        val third = splitUpAddress[2]

        if (first != ipFirstBlock) {
          Log.d("ahi3646", "isClientWithinAddressableIpRange: Mismatch IP first block: $ip $hostName")
          return false
        }

        if (second != ipSecondBlock) {
          Log.d("ahi3646", "isClientWithinAddressableIpRange: Mismatch IP second block: $ip $hostName ")
          return false
        }

        if (third != ipThirdBlock) {
          Log.d("ahi3646", "isClientWithinAddressableIpRange: Mismatch IP third block: $ip $hostName ")
          return false
        }

        // This is a "matching" IP of
        // XXX.YYY.ZZZ.???
        // which is good enough for us
        return true
      }
    }

    data object Empty : ConnectionInfo

    data class Error
    internal constructor(
        val error: Throwable,
    ) : ConnectionInfo

    @CheckResult
    fun update(onUpdate: (Connected) -> Connected): ConnectionInfo {
      return when (this) {
        is Connected -> onUpdate(this)
        is Empty -> this
        is Error -> this
        is Unchanged -> this
      }
    }
  }
}
