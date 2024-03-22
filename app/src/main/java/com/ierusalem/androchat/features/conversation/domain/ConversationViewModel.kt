package com.ierusalem.androchat.features.conversation.domain

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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

    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    fun connectToChat() {
        getAllMessages()
        savedStateHandle.get<String>("username")?.let { username ->
            viewModelScope.launch {
                val result = chatSocketService.initSession(username)
                when (result) {
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
    }

    fun onMessageChange(message: String) {
        _messageText.value = message
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

    fun getAllMessages() {
        viewModelScope.launch {
            _state.value = state.value.copy(isLoading = true)
            val result = messageService.getAllMessages()
            _state.value = state.value.copy(
                messages = result,
                isLoading = false
            )
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if (messageText.value.isNotBlank()) {
                chatSocketService.sendMessage(messageText.value)
            }
        }
    }

}

data class ConversationState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)