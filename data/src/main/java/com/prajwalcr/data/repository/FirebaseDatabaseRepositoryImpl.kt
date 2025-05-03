package com.prajwalcr.data.repository

import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.prajwalcr.domain.Constants.firebaseDatabaseUrl
import com.prajwalcr.domain.model.Channel
import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDatabaseRepositoryImpl: FirebaseDatabaseRepository {

    companion object {
        const val UNKNOWN_DATABASE_KEY = "Unknown"
        const val CHANNEL_PATH = "channel"
    }

    private val database by lazy {
        try {
            Firebase.database(firebaseDatabaseUrl)
        } catch (ex: Exception) {
            Timber.e("Exception while getting firebase database instance. EX: $ex")
            null
        }
    }

    override suspend fun getChannels(): List<Channel> {
        val channelList= suspendCoroutine<List<Channel>> { continuation ->
            database?.getReference(CHANNEL_PATH)?.get()
                ?.addOnSuccessListener {
                    val channelList = it.children.map { data ->
                        Channel(
                            data.key ?: UNKNOWN_DATABASE_KEY,
                            data.value.toString()
                        )
                    }
                    continuation.resume(channelList)
                }
                ?.addOnFailureListener { error ->
                    Timber.e("Failed to get Channel list. $error")
                    continuation.resume(listOf())
                }
        }
        return channelList
    }

    override suspend fun addChannel(channelName: String): Boolean {
        return suspendCoroutine { continuation ->
            val key = database?.getReference(CHANNEL_PATH)?.push()?.key
            key?.let {
                database?.getReference(CHANNEL_PATH)?.child(it)?.setValue(channelName)
                    ?.addOnSuccessListener { continuation.resume(true) }
                    ?.addOnFailureListener { continuation.resume(false) }
            } ?: continuation.resume(false)
        }
    }
}