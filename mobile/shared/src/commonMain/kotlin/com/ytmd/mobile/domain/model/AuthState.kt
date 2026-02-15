package com.ytmd.mobile.domain.model

sealed class AuthState {

    data class Authenticated(
        val cookies: Map<String, String>,
    ) : AuthState()

    data object Unauthenticated : AuthState()
}
