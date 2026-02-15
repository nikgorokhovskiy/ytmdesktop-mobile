package com.ytmd.mobile.di

import com.ytmd.mobile.data.DatabaseDriverFactory
import com.ytmd.mobile.data.auth.CookieStore
import com.ytmd.mobile.data.auth.IosCookieStore
import com.ytmd.mobile.player.PlayerController
import org.koin.dsl.module

/**
 * iOS-specific Koin DI module.
 * Provides platform implementations for expect/actual classes and interfaces.
 */
val platformModule = module {
    single { DatabaseDriverFactory() }
    single<CookieStore> { IosCookieStore() }
    single { PlayerController() }
}
