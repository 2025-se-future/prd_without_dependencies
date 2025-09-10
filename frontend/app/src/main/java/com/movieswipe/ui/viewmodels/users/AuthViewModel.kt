package com.movieswipe.ui.viewmodels.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.movieswipe.data.models.AuthState
import com.movieswipe.data.models.User
import com.movieswipe.data.repositories.users.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn()
    val currentUserName = authRepository.getCurrentUser()

    init {
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch {
            combine(
                authRepository.isLoggedIn(),
                authRepository.getCurrentUser()
            ) { isLoggedIn, userName ->
                if (isLoggedIn && userName != null) {
                    _authState.value = AuthState.Success(
                        User(
                            id = "", // Will be fetched from preferences if needed
                            googleId = "",
                            email = "",
                            name = userName,
                            createdAt = "",
                            updatedAt = ""
                        )
                    )
                } else {
                    _authState.value = AuthState.Idle
                }
            }.collect {} // Add collect to actually start collecting
        }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _authState.value = AuthState.Loading

            try {
                val idTokenResult = authRepository.handleGoogleSignInResult(task)
                
                if (idTokenResult.isSuccess) {
                    val idToken = idTokenResult.getOrThrow()
                    authenticateWithBackend(idToken)
                } else {
                    val errorMsg = idTokenResult.exceptionOrNull()?.message ?: "Google Sign-In failed"
                    handleError(errorMsg)
                }
            } catch (e: Exception) {
                handleError(e.message ?: "Authentication failed")
            }
        }
    }

    private suspend fun authenticateWithBackend(idToken: String) {
        try {
            val result = authRepository.authenticateWithGoogle(idToken)
            
            if (result.isSuccess) {
                val authResult = result.getOrThrow()
                _authState.value = AuthState.Success(authResult.user)
                _isLoading.value = false
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Backend authentication failed"
                handleError(errorMsg)
            }
        } catch (e: Exception) {
            handleError(e.message ?: "Authentication failed")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.signOut()
                
                if (result.isSuccess) {
                    _authState.value = AuthState.Idle
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Sign out failed"
                    _errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign out failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getGoogleSignInClient() = authRepository.getGoogleSignInClient()

    fun clearError() {
        _errorMessage.value = null
    }

    private fun handleError(message: String) {
        _authState.value = AuthState.Error(message)
        _errorMessage.value = message
        _isLoading.value = false
    }
}
