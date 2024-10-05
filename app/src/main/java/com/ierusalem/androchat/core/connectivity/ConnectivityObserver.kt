package com.ierusalem.androchat.core.connectivity

import kotlinx.coroutines.flow.Flow


interface ConnectivityObserver {

    fun observe(): Flow<Status>

    fun observeWifiState(): Flow<Status>

    fun getWifiServerIpAddress(): String

    enum class Status{
        Available, Unavailable, Loosing, Lost
    }
}