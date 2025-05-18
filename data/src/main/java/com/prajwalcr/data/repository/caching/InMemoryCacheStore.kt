package com.prajwalcr.data.repository.caching

import com.prajwalcr.domain.repository.caching.CacheStore
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class InMemoryCacheStore: CacheStore {

    private data class InMemoryCache(
        val cacheData: Any,
        val expiryTime: Instant
    ) {
        fun isExpired(): Boolean = Instant.now().isAfter(expiryTime)
    }

    private val cacheMap = ConcurrentHashMap<String, InMemoryCache>()

    override suspend fun <T : Any> store(key: String, data: T, validity: Duration) {
        cacheMap[key] = InMemoryCache(
            cacheData = data,
            expiryTime = Instant.now().plusMillis(validity.inWholeMilliseconds)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> get(key: String): T? {
        val cache = cacheMap[key]
        return if (cache != null) {
            if (cache.isExpired()) {
                Timber.d("Cache expired for key: $key")
                clearCache(key)
                null
            } else {
                try {
                    cache.cacheData as? T
                } catch (ex: Exception) {
                    Timber.e("Error while casting cache data. EX: $ex")
                    null
                }
            }
        } else {
            null
        }
    }

    override suspend fun clearCache(key: String) {
        cacheMap.remove(key)
    }

    override suspend fun clearAllCache() {
        cacheMap.clear()
    }
}