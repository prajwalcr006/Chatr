package com.prajwalcr.domain.repository

import com.prajwalcr.domain.model.UserData

interface FirebaseAuthRepository {
    fun getUserData(): UserData?
}