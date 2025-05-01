package com.prajwalcr.domain.repository

import com.prajwalcr.domain.model.UserData

interface FirestoreRepository {
    suspend fun setUserDetails(userData: UserData)
}