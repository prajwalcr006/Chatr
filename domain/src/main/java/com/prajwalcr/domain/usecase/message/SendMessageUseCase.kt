package com.prajwalcr.domain.usecase.message

import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendMessageUseCase(
    private val firebaseDatabaseRepository: FirebaseDatabaseRepository
) {
    suspend operator fun invoke(channelId: String, messageContent: String, isImage: Boolean) = withContext(Dispatchers.IO) {
        firebaseDatabaseRepository.sendMessage(channelId, messageContent, isImage)
    }
}