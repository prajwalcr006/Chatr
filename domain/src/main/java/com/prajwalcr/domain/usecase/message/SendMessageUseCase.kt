package com.prajwalcr.domain.usecase.message

import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendMessageUseCase(
    private val firebaseDatabaseRepository: FirebaseDatabaseRepository
) {
    suspend operator fun invoke(channelId: String, messageText: String) = withContext(Dispatchers.IO) {
        firebaseDatabaseRepository.sendMessage(channelId, messageText)
    }
}