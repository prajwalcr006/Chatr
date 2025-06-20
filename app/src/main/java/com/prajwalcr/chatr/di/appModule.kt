package com.prajwalcr.chatr.di

import com.prajwalcr.chatr.ui.MainViewModel
import com.prajwalcr.chatr.ui.screens.chat.ChatViewModel
import com.prajwalcr.chatr.ui.screens.signin.SignInViewModel
import com.prajwalcr.chatr.ui.screens.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        SignInViewModel()
    }

    viewModel {
        MainViewModel(
            get()
        )
    }

    viewModel {
        HomeViewModel(
            get(),
            get()
        )
    }

    viewModel {
        ChatViewModel(
            get(),
            get(),
            get()
        )
    }
}