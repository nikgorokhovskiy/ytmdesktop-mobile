package com.ytmd.mobile.android

import android.app.Application
import com.ytmd.mobile.di.platformModule
import com.ytmd.mobile.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class YtmdApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@YtmdApp)
            modules(sharedModule, platformModule)
        }
    }
}
