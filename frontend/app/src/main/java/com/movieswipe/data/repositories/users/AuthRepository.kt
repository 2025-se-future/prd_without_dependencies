package com.movieswipe.data.repositories.users

import com.movieswipe.data.local.PreferencesManager
import com.movieswipe.data.local.TokenManager
import com.movieswipe.data.models.AuthRequest
import com.movieswipe.data.models.AuthResult
import com.movieswipe.data.models.ErrorResponse
import com.movieswipe.data.models.User
import com.movieswipe.data.remote.AuthApi
import com.movieswipe.data.remote.GoogleSignInHelper
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager,
    private val googleSignInHelper: GoogleSignInHelper,
    private val moshi: Moshi
) {
    fun isLoggedIn(): Flow<Boolean> = tokenManager.isLoggedIn()

    fun getCurrentUser(): Flow<String?> = preferencesManager.getUserName()

    suspend fun authenticateWithGoogle(idToken: String): Result<AuthResult> {
        return try {
            val request = AuthRequest(idToken = idToken)
            val response = authApi.authenticateWithGoogle(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                val authResult = authResponse?.data
                
                if (authResult != null) {
                    // Save authentication data
                    saveAuthenticationData(authResult.token, authResult.user)
                    Result.success(authResult)
                } else {
                    Result.failure(Exception("Authentication failed: No data received"))
                }
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = parseErrorMessage(e.response()?.errorBody()?.string())
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveAuthenticationData(token: String, user: User) {
        tokenManager.saveToken(token)
        tokenManager.saveUserId(user.id)
        tokenManager.setLoggedIn(true)
        preferencesManager.saveUserName(user.name)
        preferencesManager.saveUserEmail(user.email)
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            googleSignInHelper.signOut()
            tokenManager.clearAllData()
            preferencesManager.clearUserData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentToken(): String? {
        return tokenManager.getToken().first()
    }

    fun getGoogleSignInClient() = googleSignInHelper.getSignInClient()

    fun handleGoogleSignInResult(task: com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>): Result<String> {
        return googleSignInHelper.handleSignInResult(task)
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val adapter = moshi.adapter(ErrorResponse::class.java)
                val errorResponse = adapter.fromJson(errorBody)
                errorResponse?.message ?: "Unknown error occurred"
            } else {
                "Unknown error occurred"
            }
        } catch (e: Exception) {
            "Failed to parse error response"
        }
    }
}
