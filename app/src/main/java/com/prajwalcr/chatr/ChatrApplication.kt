package com.prajwalcr.chatr

import android.app.Application
import com.prajwalcr.chatr.di.appModule
import com.prajwalcr.di.KoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class ChatrApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@ChatrApplication)
            modules(
                KoinModule.dataModule + KoinModule.domainModule +
                appModule
            )
        }

    }
}