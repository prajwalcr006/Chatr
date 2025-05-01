package com.prajwalcr.domain.repository

import com.prajwalcr.domain.model.Channel

interface FirebaseDatabaseRepository {
    suspend fun getChannels(): List<Channel>
}