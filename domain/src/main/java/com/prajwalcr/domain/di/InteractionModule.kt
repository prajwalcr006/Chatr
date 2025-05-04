package com.prajwalcr.domain.di

import com.prajwalcr.domain.usecase.AddChannelUseCase
import com.prajwalcr.domain.usecase.GetChannelsUseCase
import com.prajwalcr.domain.usecase.ListenForMessagesUseCase
import com.prajwalcr.domain.usecase.SetUserDataToFirebaseUserCase
import org.koin.dsl.module

val interactionModule = module {
    factory { SetUserDataToFirebaseUserCase(get()) }
    factory { GetChannelsUseCase(get()) }
    factory { AddChannelUseCase(get()) }
    factory { ListenForMessagesUseCase(get()) }
}