package com.ytmd.mobile.domain.repository

import com.ytmd.mobile.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun getAuthState(): Flow<AuthState>

    suspend fun saveCookies(cookies: Map<String, String>)

    suspend fun clearAuth()

    suspend fun buildAuthHeaders(): Map<String, String>
}
