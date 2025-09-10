package com.movieswipe.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieswipe.data.repositories.UsersRepository
import com.movieswipe.utils.AuthResult
import com.movieswipe.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val authResult: AuthResult? = null,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun authenticateWithGoogle(idToken: String) {
        Log.d("UsersViewModel", "Authenticating with Google, idToken: $idToken")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            Log.d("UsersViewModel", "Calling usersRepository.authenticateWithGoogle")
            when (val result = usersRepository.authenticateWithGoogle(idToken)) {
                is Result.Success -> {
                    Log.d("UsersViewModel", "Success data: ${result.data}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        authResult = result.data,
                        isAuthenticated = true,
                        errorMessage = null
                    )
                }
                is Result.Error -> {
                    Log.d("UsersViewModel", "Error message: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isAuthenticated = false
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
