package com.prajwalcr.chatr.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.usecase.message.ListenForMessagesUseCase
import com.prajwalcr.domain.usecase.message.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatViewModel(
    private val listenForMessagesUseCase: ListenForMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
): ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun sendMessage(channelId: String, messageText: String) {
        viewModelScope.launch {
            try {
                sendMessageUseCase(channelId, messageText)
            } catch (ex: Exception) {
                Timber.e("Exception in sending message. EX: $ex")
            }
        }
    }

    fun listenForMessages(channelId: String) {
        viewModelScope.launch {
            try {
                listenForMessagesUseCase(channelId).collect {
                    _messages.value = it
                }
            } catch (ex: Exception) {
                Timber.e("Exception in listening for messages. EX: $ex")
            }
        }
    }
}