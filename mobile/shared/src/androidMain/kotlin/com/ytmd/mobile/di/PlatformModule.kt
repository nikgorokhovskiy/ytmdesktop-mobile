package com.ytmd.mobile.di

import android.content.Context
import com.ytmd.mobile.data.DatabaseDriverFactory
import com.ytmd.mobile.data.auth.AndroidCookieStore
import com.ytmd.mobile.data.auth.CookieStore
import com.ytmd.mobile.player.PlayerController
import org.koin.dsl.module

/**
 * Android-specific Koin DI module.
 * Provides platform implementations for expect/actual classes and interfaces.
 *
 * Requires a Context instance registered in Koin (done via androidContext() in YtmdApp).
 */
val platformModule = module {
    single { DatabaseDriverFactory(get<Context>()) }
    single<CookieStore> { AndroidCookieStore(get<Context>()) }
    single { PlayerController(get<Context>()) }
}
