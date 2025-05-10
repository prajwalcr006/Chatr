package com.prajwalcr.data.repository


import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.FirebaseAuthRepository
import timber.log.Timber

class FirebaseAuthRepositoryImpl: FirebaseAuthRepository {

    private val auth by lazy {
        try {
            Firebase.auth
        } catch (ex: Exception) {
            Timber.e("Firebase auth failed!! EX: $ex")
            null
        }
    }

    override fun getUserData(): UserData? = auth?.currentUser?.run {
        UserData(
            email = email.toString(),
            userId = uid,
            userName = displayName.toString(),
            profileUrl = photoUrl.toString().substring(0,photoUrl.toString().length - 6)
        )

    }
}