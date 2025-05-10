package com.prajwalcr.domain.repository.caching

import kotlin.time.Duration

interface CacheStore {
    suspend fun <T: Any> store(key: String, data: T, validity: Duration)
    suspend fun <T: Any> get(key: String): T?
    suspend fun clearCache(key: String)
    suspend fun clearAllCache()
}