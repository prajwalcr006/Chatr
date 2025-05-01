package com.prajwalcr.data.di

import com.prajwalcr.data.repository.FirebaseDatabaseRepositoryImpl
import com.prajwalcr.data.repository.FirestoreRepositoryImpl
import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import com.prajwalcr.domain.repository.FirestoreRepository
import org.koin.dsl.module

val FirebaseModule = module {
    single<FirestoreRepository> {
        FirestoreRepositoryImpl()
    }

    single<FirebaseDatabaseRepository> {
        FirebaseDatabaseRepositoryImpl()
    }
}