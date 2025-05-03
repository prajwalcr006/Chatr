package com.prajwalcr.domain.usecase

import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddChannelUseCase(
    private val firebaseDatabaseRepository: FirebaseDatabaseRepository
) {
    suspend operator fun invoke(channelName: String) = withContext(Dispatchers.IO) {
        firebaseDatabaseRepository.addChannel(channelName)
    }
}