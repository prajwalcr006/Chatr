package com.prajwalcr.domain.di

import com.prajwalcr.domain.usecase.channel.AddChannelUseCase
import com.prajwalcr.domain.usecase.channel.GetChannelsUseCase
import com.prajwalcr.domain.usecase.message.ListenForMessagesUseCase
import com.prajwalcr.domain.usecase.message.SendMessageUseCase
import com.prajwalcr.domain.usecase.message.UploadImageUseCase
import com.prajwalcr.domain.usecase.user.SetUserDataToFirebaseUserCase
import org.koin.dsl.module

val interactionModule = module {
    factory { SetUserDataToFirebaseUserCase(get()) }
    factory { GetChannelsUseCase(get()) }
    factory { AddChannelUseCase(get()) }
    factory { ListenForMessagesUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { UploadImageUseCase(get()) }
}