package com.prajwalcr.domain.repository

import com.prajwalcr.domain.model.UserData

interface FirebaseAuthRepository {
    suspend fun getUserData(): UserData?
    suspend fun getUserName(): String
    suspend fun getUserId(): String
    suspend fun getProfileUrl(): String
}
