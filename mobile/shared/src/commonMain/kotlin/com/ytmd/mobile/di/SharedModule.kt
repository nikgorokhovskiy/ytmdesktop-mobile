package com.ytmd.mobile.di

import com.ytmd.mobile.data.auth.AuthManager
import com.ytmd.mobile.data.remote.InnertubeApi
import com.ytmd.mobile.data.repository.MusicRepositoryImpl
import com.ytmd.mobile.data.repository.OfflineRepositoryImpl
import com.ytmd.mobile.domain.repository.AuthRepository
import com.ytmd.mobile.domain.repository.MusicRepository
import com.ytmd.mobile.domain.repository.OfflineRepository
import com.ytmd.mobile.domain.repository.PlayerRepository
import com.ytmd.mobile.domain.usecase.GetPlaylistsUseCase
import com.ytmd.mobile.domain.usecase.GetStreamUseCase
import com.ytmd.mobile.domain.usecase.SearchUseCase
import com.ytmd.mobile.domain.usecase.ToggleLikeUseCase
import com.ytmd.mobile.player.PlayerStateManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared Koin DI module containing common dependencies.
 * Platform-specific bindings (PlayerController, CookieStore, DatabaseDriver)
 * are provided in platformModule().
 */
val sharedModule: Module = module {

    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    // Ktor HttpClient
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                level = LogLevel.HEADERS
            }
        }
    }

    // Auth
    single<AuthRepository> { AuthManager(cookieStore = get()) }

    // API
    single { InnertubeApi(httpClient = get(), authRepository = get()) }

    // Repositories
    single<MusicRepository> { MusicRepositoryImpl(innertubeApi = get()) }
    single<OfflineRepository> { OfflineRepositoryImpl(trackDao = get()) }
    single<PlayerRepository> {
        PlayerStateManager(
            playerController = get(),
            getStreamUrl = { videoId ->
                get<MusicRepository>().getStreamInfo(videoId)?.url
            },
        )
    }

    // Use Cases
    factory { GetStreamUseCase(musicRepository = get()) }
    factory { SearchUseCase(musicRepository = get()) }
    factory { GetPlaylistsUseCase(musicRepository = get()) }
    factory { ToggleLikeUseCase(musicRepository = get()) }
}
