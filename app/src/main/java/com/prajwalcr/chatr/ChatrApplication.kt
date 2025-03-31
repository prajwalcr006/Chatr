package com.prajwalcr.chatr

import android.app.Application
import com.prajwalcr.chatr.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatrApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ChatrApplication)
            modules(
                appModule
            )
        }

    }
}