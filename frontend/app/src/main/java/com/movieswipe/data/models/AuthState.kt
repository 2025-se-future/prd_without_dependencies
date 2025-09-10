package com.movieswipe.data.models

sealed class AuthState {
    object Loading : AuthState()
    object Idle : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Note: AuthResult data class is defined in AuthResponse.kt
