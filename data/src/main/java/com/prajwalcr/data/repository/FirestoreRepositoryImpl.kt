package com.prajwalcr.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prajwalcr.domain.Constants.USER_COLLECTION
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.FirestoreRepository
import timber.log.Timber

class FirestoreRepositoryImpl: FirestoreRepository {

    companion object {
        private const val FIREBASE_USER_ID = "userId"
        private const val FIREBASE_USER_NAME = "userName"
        private const val FIREBASE_EMAIL = "email"
        private const val FIREBASE_PROFILE_URL = "profileUrl"
    }

    private val firestore by lazy {
        try {
            Firebase.firestore
        } catch (ex: Exception) {
            Timber.e("Exception while getting firestore instance. EX: $ex")
            null
        }
    }

    override suspend fun setUserDetails(userData: UserData) {

        val userDataMap = mapOf(
            FIREBASE_USER_ID to userData.userId,
            FIREBASE_USER_NAME to userData.userName,
            FIREBASE_EMAIL to userData.email,
            FIREBASE_PROFILE_URL to userData.profileUrl
        )

        val userDocument = firestore?.collection(USER_COLLECTION)?.document(userData.userId)

        userDocument?.get()
            ?.addOnSuccessListener {
                if (it.exists()) {
                    userDocument.update(userDataMap)
                        .addOnSuccessListener {
                            Timber.i("Userdata: $userData is successfully updated to firebase firestore")
                        }
                        .addOnFailureListener {
                            Timber.e("Failed to update $userData to firebase firestore")
                        }
                } else {
                    userDocument.set(userData)
                        .addOnSuccessListener {
                            Timber.i("Userdata: $userData is successfully set to firebase firestore")
                        }
                        .addOnFailureListener {
                            Timber.e("Failed to set $userData to firebase firestore")
                        }
                }
            }
            ?.addOnFailureListener {
                Timber.e("Failed to get collection from firebase firestore")
            }

    }
}