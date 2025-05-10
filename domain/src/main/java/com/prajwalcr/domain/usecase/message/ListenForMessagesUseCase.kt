package com.prajwalcr.domain.usecase.message

import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ListenForMessagesUseCase(
    private val firebaseDatabaseRepository: FirebaseDatabaseRepository
) {
    suspend operator fun invoke(channelId: String): Flow<List<Message>> = withContext(Dispatchers.IO) {
        firebaseDatabaseRepository.listenForMessages(channelId)
            .catch {
                Timber.e("Error while listening for messages. EX: $it")
            }
    }
}