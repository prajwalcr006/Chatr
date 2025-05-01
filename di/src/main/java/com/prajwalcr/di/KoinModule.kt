package com.prajwalcr.di

import com.prajwalcr.data.di.FirebaseModule
import com.prajwalcr.domain.di.interactionModule

object KoinModule {
    val dataModule = listOf(FirebaseModule)
    val domainModule = listOf(interactionModule)
}