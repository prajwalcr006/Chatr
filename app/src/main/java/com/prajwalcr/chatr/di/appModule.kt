package com.prajwalcr.chatr.di

import com.prajwalcr.chatr.ui.screens.SignInViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        SignInViewModel()
    }
}