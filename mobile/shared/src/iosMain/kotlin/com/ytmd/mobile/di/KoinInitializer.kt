package com.ytmd.mobile.di

import org.koin.core.context.startKoin

/**
 * Koin initializer callable from Swift.
 * Called from iOSApp.swift on app launch.
 */
fun initKoin() {
    startKoin {
        modules(sharedModule, platformModule)
    }
}
