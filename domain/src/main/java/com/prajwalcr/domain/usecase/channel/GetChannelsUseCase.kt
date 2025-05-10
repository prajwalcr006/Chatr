package com.prajwalcr.domain.usecase.channel

import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetChannelsUseCase(
    private val firebaseDatabaseRepository: FirebaseDatabaseRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        firebaseDatabaseRepository.getChannels()
    }
}