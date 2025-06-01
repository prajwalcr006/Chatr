package com.prajwalcr.data.repository

import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.prajwalcr.domain.Constants.firebaseDatabaseUrl
import com.prajwalcr.domain.model.Channel
import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.repository.FirebaseAuthRepository
import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDatabaseRepositoryImpl(
    private val firebaseAuthRepository: FirebaseAuthRepository
): FirebaseDatabaseRepository {

    companion object {
        const val NOT_APPLICABLE = "NA"
        const val UNKNOWN_FIELD = "Unknown"
        const val CHANNEL_PATH = "channel"
        const val MESSAGE_PATH = "messages"
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

    private fun getMessagesDatabaseReference() = database?.getReference(MESSAGE_PATH)

    private fun getChannelsDatabaseReference() = database?.getReference(CHANNEL_PATH)

    override suspend fun getChannels(): List<Channel> {
        val channelList= suspendCoroutine<List<Channel>> { continuation ->
            database?.getReference(CHANNEL_PATH)?.get()
                ?.addOnSuccessListener {
                    val channelList = it.children.map { data ->
                        Channel(
                            data.key ?: UNKNOWN_FIELD,
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
            val key = getChannelsDatabaseReference()?.push()?.key
            key?.let {
                database?.getReference(CHANNEL_PATH)?.child(it)?.setValue(channelName)
                    ?.addOnSuccessListener { continuation.resume(true) }
                    ?.addOnFailureListener { continuation.resume(false) }
            } ?: continuation.resume(false)
        }
    }

    override suspend fun listenForMessages(channelId: String): Flow<List<Message>> = callbackFlow {
        val messageReference = getMessagesDatabaseReference()?.child(channelId)?.orderByChild(TIME)
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
    }.flowOn(Dispatchers.IO)

    override suspend fun sendMessage(channelId: String, messageContent: String, isImage:Boolean) {
        val key = getMessagesDatabaseReference()?.push()?.key
        val userData = firebaseAuthRepository.getUserData()
        val message = Message(
            messageId = key ?: UUID.randomUUID().toString(),
            text = if (!isImage) {messageContent} else NOT_APPLICABLE,
            senderId = userData?.userId ?: UNKNOWN_FIELD,
            senderName = userData?.userName ?: UNKNOWN_FIELD,
            profileUrl = userData?.profileUrl,
            createdAt = System.currentTimeMillis(),
            imageUrl = if (isImage) {messageContent} else null
        )

        getMessagesDatabaseReference()?.child(channelId)?.push()?.setValue(message)
    }
}
