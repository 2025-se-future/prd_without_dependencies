package com.movieswipe.data.remote

import com.movieswipe.data.models.AuthRequest
import com.movieswipe.data.models.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth")
    suspend fun authenticateWithGoogle(
        @Body request: AuthRequest
    ): Response<AuthResponse>
}
