package com.prajwalcr.domain.usecase.user

import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetUserDataToFirebaseUserCase(
    private val firestoreRepository: FirestoreRepository
) {
    suspend operator fun invoke(userData: UserData) = withContext(Dispatchers.IO) {
        firestoreRepository.setUserDetails(userData)
    }
}