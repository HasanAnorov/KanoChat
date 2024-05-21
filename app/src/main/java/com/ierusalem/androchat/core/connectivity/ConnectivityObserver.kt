package com.ierusalem.androchat.core.connectivity

import kotlinx.coroutines.flow.Flow


interface ConnectivityObserver {

    fun observe(): Flow<Status>
    enum class Status{
        Available, Unavailable, Loosing, Lost
    }
}