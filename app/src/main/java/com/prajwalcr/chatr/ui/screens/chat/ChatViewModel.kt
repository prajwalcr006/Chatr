package com.prajwalcr.chatr.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.usecase.message.ListenForMessagesUseCase
import com.prajwalcr.domain.usecase.message.SendMessageUseCase
import com.prajwalcr.domain.usecase.message.UploadImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatViewModel(
    private val listenForMessagesUseCase: ListenForMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val uploadImageUseCase: UploadImageUseCase
): ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _publicImageUrl = MutableStateFlow<String>("")
    val publicImageUrl: StateFlow<String> = _publicImageUrl

    fun sendMessage(channelId: String, messageContent: String, isImage: Boolean = false) {
        viewModelScope.launch {
            try {
                sendMessageUseCase(channelId, messageContent, isImage)
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

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val url = uploadImageUseCase(uri)
                url?.let {
                    _publicImageUrl.value = it
                }
            } catch (ex: Exception) {
                Timber.e("Exception in uploading image. EX: $ex")
            }
        }
    }
}