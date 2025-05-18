package com.prajwalcr.data.repository


import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.FirebaseAuthRepository
import com.prajwalcr.domain.repository.caching.CacheStore
import timber.log.Timber
import kotlin.time.Duration

class FirebaseAuthRepositoryImpl(
    private val inMemoryCacheStore: CacheStore
): FirebaseAuthRepository {

    companion object {
        const val KEY_SIGNED_IN_USER_DATA = "userData"
        const val UNKNOWN_FIELD = "Unknown"
    }

    private val auth by lazy {
        try {
            Firebase.auth
        } catch (ex: Exception) {
            Timber.e("Firebase auth failed!! EX: $ex")
            null
        }
    }

    override suspend fun getUserData(): UserData? {
        val userData = inMemoryCacheStore.get(KEY_SIGNED_IN_USER_DATA) ?: (
                auth?.currentUser?.run {
                    UserData(
                        email = email.toString(),
                        userId = uid,
                        userName = displayName.toString(),
                        profileUrl = photoUrl.toString().substring(0,photoUrl.toString().length - 6)
                    )
                }.also { userData ->
                    userData?.let {
                        inMemoryCacheStore.store(
                            KEY_SIGNED_IN_USER_DATA,
                            it,
                            Duration.INFINITE
                        )
                    }
                })

        Timber.d("userData is $userData")
        return userData
    }

    override suspend fun getUserName(): String = getUserData()?.userName ?: UNKNOWN_FIELD

    override suspend fun getUserId(): String = getUserData()?.userId ?: UNKNOWN_FIELD

    override suspend fun getProfileUrl(): String = getUserData()?.profileUrl ?: UNKNOWN_FIELD
}