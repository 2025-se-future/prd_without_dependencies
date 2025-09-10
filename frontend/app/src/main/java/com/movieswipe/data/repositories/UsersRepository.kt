package com.movieswipe.data.repositories

import com.movieswipe.data.remote.api.AuthApiService
import com.movieswipe.data.remote.mappers.toDomain
import com.movieswipe.utils.AuthResult
import com.movieswipe.utils.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(
    private val authApiService: AuthApiService
) {
    suspend fun authenticateWithGoogle(idToken: String): Result<AuthResult> {
        return when (val response = authApiService.authenticateWithGoogle(idToken)) {
            is Result.Success -> {
                response.data.data?.let { authResultDto ->
                    Result.Success(authResultDto.toDomain())
                } ?: Result.Error("Authentication failed. Please try again.")
            }
            is Result.Error -> {
                Result.Error(response.message)
            }
            is Result.Loading -> {
                Result.Loading
            }
        }
    }
}
