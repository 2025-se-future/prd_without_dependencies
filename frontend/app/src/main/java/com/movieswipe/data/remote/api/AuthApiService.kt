package com.movieswipe.data.remote.api

import android.util.Log
import com.google.gson.Gson
import com.movieswipe.data.remote.dto.AuthenticateUserRequest
import com.movieswipe.data.remote.dto.AuthenticateUserResponse
import com.movieswipe.data.remote.dto.ErrorResponse
import com.movieswipe.utils.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApiService @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:3000/api"
        private const val AUTH_ENDPOINT = "$BASE_URL/auth"
    }
    
    suspend fun authenticateWithGoogle(idToken: String): Result<AuthenticateUserResponse> {
        return try {
            val request = AuthenticateUserRequest(idToken)
            val json = gson.toJson(request)
            val requestBody = json.toRequestBody("application/json".toMediaType())
            
            val httpRequest = Request.Builder()
                .url(AUTH_ENDPOINT)
                .post(requestBody)
                .build()
            
            val response = httpClient.newCall(httpRequest).execute()
            Log.d("AuthApiService", "Response code: ${response.code}")
            handleAuthResponse(response)
        } catch (e: IOException) {
            Result.Error("Authentication service temporarily unavailable. Please try again.")
        } catch (e: Exception) {
            Result.Error("Authentication unsuccessful. Please try again.")
        }
    }
    
    private fun handleAuthResponse(response: Response): Result<AuthenticateUserResponse> {
        val responseBody = response.body?.string()
        
        return when (response.code) {
            201 -> {
                try {
                    val authResponse = gson.fromJson(responseBody, AuthenticateUserResponse::class.java)
                    Result.Success(authResponse)
                } catch (e: Exception) {
                    Result.Error("Failed to process authentication response")
                }
            }
            400, 401 -> {
                try {
                    val errorResponse = gson.fromJson(responseBody, ErrorResponse::class.java)
                    Result.Error(errorResponse.message)
                } catch (e: Exception) {
                    Result.Error("Authentication unsuccessful. Please try again.")
                }
            }
            else -> {
                Result.Error("Authentication service temporarily unavailable. Please try again.")
            }
        }
    }
}
