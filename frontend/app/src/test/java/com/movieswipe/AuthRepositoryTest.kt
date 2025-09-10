package com.movieswipe

import com.movieswipe.data.local.PreferencesManager
import com.movieswipe.data.local.TokenManager
import com.movieswipe.data.models.AuthRequest
import com.movieswipe.data.models.AuthResponse
import com.movieswipe.data.models.AuthResult
import com.movieswipe.data.models.ErrorResponse
import com.movieswipe.data.models.User
import com.movieswipe.data.remote.AuthApi
import com.movieswipe.data.remote.GoogleSignInHelper
import com.movieswipe.data.repositories.users.AuthRepository
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Response

/**
 * Unit tests for AuthRepository
 * 
 * These tests verify the data layer logic for authentication.
 */
class AuthRepositoryTest {

    private lateinit var authApi: AuthApi
    private lateinit var tokenManager: TokenManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var moshi: Moshi
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        authApi = mockk()
        tokenManager = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)
        googleSignInHelper = mockk()
        moshi = Moshi.Builder().build()
        
        authRepository = AuthRepository(
            authApi = authApi,
            tokenManager = tokenManager,
            preferencesManager = preferencesManager,
            googleSignInHelper = googleSignInHelper,
            moshi = moshi
        )
    }

    @Test
    fun `authenticateWithGoogle should return success on successful API call`() = runTest {
        // Given: Successful API response
        val idToken = "valid_id_token"
        val user = User("123", "456", "john@example.com", "John Doe", "2024-01-01", "2024-01-01")
        val authResult = AuthResult(token = "jwt_token", user = user)
        val authResponse = AuthResponse(message = "Success", data = authResult)
        val apiResponse = Response.success(authResponse)

        coEvery { authApi.authenticateWithGoogle(AuthRequest(idToken)) } returns apiResponse

        // When: Authenticating with Google
        val result = authRepository.authenticateWithGoogle(idToken)

        // Then: Result should be success
        assertTrue(result.isSuccess)
        assertEquals(authResult, result.getOrNull())
        
        // Verify data was saved
        coVerify { tokenManager.saveToken("jwt_token") }
        coVerify { tokenManager.saveUserId("123") }
        coVerify { tokenManager.setLoggedIn(true) }
        coVerify { preferencesManager.saveUserName("John Doe") }
        coVerify { preferencesManager.saveUserEmail("john@example.com") }
    }

    @Test
    fun `authenticateWithGoogle should return failure on API error`() = runTest {
        // Given: API error response
        val idToken = "invalid_id_token"
        val errorBody = """{"message":"Invalid Google token"}"""
        val apiResponse = Response.error<AuthResponse>(
            401,
            errorBody.toResponseBody("application/json".toMediaTypeOrNull())
        )

        coEvery { authApi.authenticateWithGoogle(AuthRequest(idToken)) } returns apiResponse

        // When: Authenticating with Google
        val result = authRepository.authenticateWithGoogle(idToken)

        // Then: Result should be failure
        assertTrue(result.isFailure)
        assertEquals("Invalid Google token", result.exceptionOrNull()?.message)
    }

    @Test
    fun `authenticateWithGoogle should return failure on null response data`() = runTest {
        // Given: API response with null data
        val idToken = "valid_id_token"
        val authResponse = AuthResponse(message = "Success", data = null)
        val apiResponse = Response.success(authResponse)

        coEvery { authApi.authenticateWithGoogle(AuthRequest(idToken)) } returns apiResponse

        // When: Authenticating with Google
        val result = authRepository.authenticateWithGoogle(idToken)

        // Then: Result should be failure
        assertTrue(result.isFailure)
        assertEquals("Authentication failed: No data received", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signOut should clear all data on successful sign out`() = runTest {
        // Given: Successful sign out setup
        coEvery { googleSignInHelper.signOut() } returns Unit

        // When: Signing out
        val result = authRepository.signOut()

        // Then: Result should be success and data should be cleared
        assertTrue(result.isSuccess)
        coVerify { googleSignInHelper.signOut() }
        coVerify { tokenManager.clearAllData() }
        coVerify { preferencesManager.clearUserData() }
    }

    @Test
    fun `signOut should return failure on exception`() = runTest {
        // Given: Sign out throws exception
        val exception = Exception("Sign out failed")
        coEvery { googleSignInHelper.signOut() } throws exception

        // When: Signing out
        val result = authRepository.signOut()

        // Then: Result should be failure
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `isLoggedIn should return flow from token manager`() {
        // Given: Token manager returns logged in flow
        val expectedFlow = flowOf(true)
        every { tokenManager.isLoggedIn() } returns expectedFlow

        // When: Getting logged in status
        val result = authRepository.isLoggedIn()

        // Then: Should return the same flow
        assertEquals(expectedFlow, result)
    }

    @Test
    fun `getCurrentUser should return flow from preferences manager`() {
        // Given: Preferences manager returns user name flow
        val expectedFlow = flowOf("John Doe")
        every { preferencesManager.getUserName() } returns expectedFlow

        // When: Getting current user
        val result = authRepository.getCurrentUser()

        // Then: Should return the same flow
        assertEquals(expectedFlow, result)
    }

    @Test
    fun `getCurrentToken should return token from token manager`() = runTest {
        // Given: Token manager returns token
        val expectedToken = "jwt_token"
        every { tokenManager.getToken() } returns flowOf(expectedToken)

        // When: Getting current token
        val result = authRepository.getCurrentToken()

        // Then: Should return the token
        assertEquals(expectedToken, result)
    }

    @Test
    fun `getGoogleSignInClient should delegate to helper`() {
        // Given: Google sign in helper setup
        val mockClient = mockk<com.google.android.gms.auth.api.signin.GoogleSignInClient>()
        every { googleSignInHelper.getSignInClient() } returns mockClient

        // When: Getting Google Sign-In client
        val result = authRepository.getGoogleSignInClient()

        // Then: Should return the client from helper
        assertEquals(mockClient, result)
    }

    @Test
    fun `handleGoogleSignInResult should delegate to helper`() {
        // Given: Google sign in helper setup
        val mockTask = mockk<com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>>()
        val expectedResult = Result.success("id_token")
        every { googleSignInHelper.handleSignInResult(mockTask) } returns expectedResult

        // When: Handling Google Sign-In result
        val result = authRepository.handleGoogleSignInResult(mockTask)

        // Then: Should return the result from helper
        assertEquals(expectedResult, result)
    }

    @Test
    fun `parseErrorMessage should parse error response correctly`() = runTest {
        // Given: Error response with structured error
        val idToken = "invalid_id_token"
        val errorBody = """{"message":"Validation error","errors":[{"field":"idToken","message":"Google token is required"}]}"""
        val apiResponse = Response.error<AuthResponse>(
            400,
            errorBody.toResponseBody("application/json".toMediaTypeOrNull())
        )

        coEvery { authApi.authenticateWithGoogle(AuthRequest(idToken)) } returns apiResponse

        // When: Authenticating with Google
        val result = authRepository.authenticateWithGoogle(idToken)

        // Then: Should parse error message correctly
        assertTrue(result.isFailure)
        assertEquals("Validation error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `parseErrorMessage should handle malformed error response`() = runTest {
        // Given: Malformed error response
        val idToken = "invalid_id_token"
        val errorBody = """{"invalid_json":}"""
        val apiResponse = Response.error<AuthResponse>(
            400,
            errorBody.toResponseBody("application/json".toMediaTypeOrNull())
        )

        coEvery { authApi.authenticateWithGoogle(AuthRequest(idToken)) } returns apiResponse

        // When: Authenticating with Google
        val result = authRepository.authenticateWithGoogle(idToken)

        // Then: Should handle malformed JSON gracefully
        assertTrue(result.isFailure)
        assertEquals("Failed to parse error response", result.exceptionOrNull()?.message)
    }
}
