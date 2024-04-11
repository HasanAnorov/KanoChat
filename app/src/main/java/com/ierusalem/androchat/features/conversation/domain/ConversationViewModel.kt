package com.ierusalem.androchat.features.conversation.domain

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ierusalem.androchat.features.auth.register.data.remote.MessageService
import com.ierusalem.androchat.features.auth.register.domain.model.Message
import com.ierusalem.androchat.features.conversation.data.remote.ChatSocketService
import com.ierusalem.androchat.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val messageService: MessageService,
    private val chatSocketService: ChatSocketService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state: MutableStateFlow<ConversationState> = MutableStateFlow(
        ConversationState()
    )
    val state = _state.asStateFlow()

    fun handleIntents(event: ConversationEvents) {
        when (event) {
            is ConversationEvents.SendMessage -> {
                Log.d("ahi3646", "handleIntents: send message ${event.message} ")
                sendMessage(event.message)
            }
            ConversationEvents.NavigateToProfile -> {}
            ConversationEvents.NavIconClick -> {}
        }
    }

    fun connectToChat(username: String) {
        Log.d("ahi3646", "handleIntents: send message ${savedStateHandle.get<String>("username")} ")
        getAllMessages()
        viewModelScope.launch {
            when (val result = chatSocketService.initSession(username)) {
                is Resource.Loading -> {
                    Log.d("ahi3646", "loading: ")
                }

                is Resource.Success -> {
                    chatSocketService.observerMessages()
                        .onEach { message ->
                            val newList = state.value.messages.toMutableList().apply {
                                add(0, message)
                            }
                            _state.value = state.value.copy(
                                messages = newList
                            )
                        }.launchIn(viewModelScope)
                }

                is Resource.Failure -> {
                    //user emit navigation here
                    Log.d("ahi3646", "error: ${result.errorMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            chatSocketService.closeSession()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    private fun getAllMessages() {
        viewModelScope.launch {
            _state.value = state.value.copy(isLoading = true)
            val result = messageService.getAllMessages()
            _state.value = state.value.copy(
                messages = result,
                isLoading = false
            )
        }
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch {
            chatSocketService.sendMessage(message)
        }
    }

}

data class ConversationState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)