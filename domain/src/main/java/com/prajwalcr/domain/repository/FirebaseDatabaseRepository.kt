package com.prajwalcr.domain.repository

import com.prajwalcr.domain.model.Channel
import com.prajwalcr.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface FirebaseDatabaseRepository {
    suspend fun getChannels(): List<Channel>
    suspend fun addChannel(channelName: String): Boolean
    suspend fun listenForMessages(channelId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message): Boolean
}