package com.ierusalem.androchat.utils

import android.util.Log
import com.ierusalem.androchat.features_tcp.server.broadcast.BroadcastEvent
import com.ierusalem.androchat.features_tcp.server.broadcast.wifidirect.WiFiNetworkEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class EventBus {
    private val _eventFlow = MutableSharedFlow<WiFiNetworkEvent>()

    fun subscribe(scope: CoroutineScope, block: suspend (WiFiNetworkEvent) -> Unit) =
        _eventFlow.onEach(block).launchIn(scope)

    suspend fun emitEvent(event: WiFiNetworkEvent) {
        Log.d(TAG, "Emitting event = $event")
        _eventFlow.emit(event)
    }

    fun getAsBroadcastEvent(): Flow<BroadcastEvent>{
        return _eventFlow.map { event ->
            when(event){
                is WiFiNetworkEvent.ConnectionChanged -> {
                    BroadcastEvent.ConnectionChanged(
                        hostName = event.hostName,
                    )
                }
                else -> {BroadcastEvent.Other}
            }
        }
    }

    companion object {
        private const val TAG = "EventBus"
    }
}


//object KotlinBus {
//    private val _events = MutableSharedFlow<WiFiNetworkEvent>()
//    val events = _events.asSharedFlow()
//
//    suspend fun publish(event: WiFiNetworkEvent) {
//        _events.emit(event)
//    }
//
//    suspend inline fun <reified T> subscribe(crossinline onEvent: (T) -> Unit) {
//        events.filterIsInstance<T>()
//            .collectLatest { event ->
//                coroutineContext.ensureActive()
//                onEvent(event)
//            }
//    }
//
//    fun getAsBroadcastEvent(): Flow<BroadcastEvent>{
//        return _events.map {event ->
//            when(event){
//                is WiFiNetworkEvent.ConnectionChanged -> {
//                    BroadcastEvent.ConnectionChanged(
//                        hostName = event.hostName,
//                    )
//                }
//                else -> {BroadcastEvent.Other}
//            }
//        }
//    }
//}