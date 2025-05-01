package com.prajwalcr.domain.di

import com.prajwalcr.domain.usecase.SetUserDataToFirebaseUserCase
import org.koin.dsl.module

val interactionModule = module {
    factory { SetUserDataToFirebaseUserCase(get()) }
}