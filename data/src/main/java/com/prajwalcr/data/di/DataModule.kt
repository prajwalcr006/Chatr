package com.prajwalcr.data.di

import com.prajwalcr.data.repository.FirebaseAuthRepositoryImpl
import com.prajwalcr.data.repository.FirebaseDatabaseRepositoryImpl
import com.prajwalcr.data.repository.FirestoreRepositoryImpl
import com.prajwalcr.data.repository.SupabaseRepositoryImpl
import com.prajwalcr.data.repository.caching.InMemoryCacheStore
import com.prajwalcr.domain.repository.FirebaseAuthRepository
import com.prajwalcr.domain.repository.FirebaseDatabaseRepository
import com.prajwalcr.domain.repository.FirestoreRepository
import com.prajwalcr.domain.repository.caching.CacheStore
import com.prajwalcr.domain.repository.caching.SupabaseRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val FirebaseModule = module {
    single<FirestoreRepository> {
        FirestoreRepositoryImpl()
    }

    single<FirebaseDatabaseRepository> {
        FirebaseDatabaseRepositoryImpl(get())
    }

    single<FirebaseAuthRepository> { FirebaseAuthRepositoryImpl(get()) }

    single<CacheStore> { InMemoryCacheStore() }

    single<SupabaseRepository> { SupabaseRepositoryImpl(androidContext()) }
}