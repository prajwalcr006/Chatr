package com.prajwalcr.data.repository

import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.prajwalcr.domain.Constants.firebaseDatabaseUrl
import com.prajwalcr.domain.model.Channel
import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDatabaseRepositoryImpl: FirebaseDatabaseRepository {

    companion object {
        const val UNKNOWN_DATABASE_KEY = "Unknown"
        const val CHANNEL_PATH = "channel"
        const val MESSAGE_PATH = "message"
        const val TIME = "createdAt"
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

    override suspend fun listenForMessages(channelId: String): Flow<List<Message>> = callbackFlow {
        val messageReference = database?.getReference(MESSAGE_PATH)?.child(channelId)?.orderByChild(TIME)
        val listener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList : MutableList<Message> = mutableListOf()
                snapshot.children.forEach { data ->
                    val message = data.getValue(Message::class.java)
                    message?.let {
                        messageList.add(it)
                    }
                }
                trySend(messageList)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("onCancelled: Listening on message db changes. error = $error")
            }
        }

        try {
            messageReference?.addValueEventListener(listener)
        } catch (ex: Exception) {
            Timber.e("Exception in listening for message database change. EX: $ex")
        }

        awaitClose {
            try {
                messageReference?.removeEventListener(listener)
            } catch (ex: Exception) {
                Timber.e("Exception in closing the message database listener. EX: $ex")
            }
        }
    }

    override suspend fun sendMessage(message: Message): Boolean {
        TODO("Not yet implemented")
    }
}