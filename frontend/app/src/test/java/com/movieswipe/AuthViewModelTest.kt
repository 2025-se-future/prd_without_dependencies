package com.movieswipe

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.movieswipe.data.models.AuthResult
import com.movieswipe.data.models.AuthState
import com.movieswipe.data.models.User
import com.movieswipe.data.repositories.users.AuthRepository
import com.movieswipe.ui.viewmodels.users.AuthViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AuthViewModel
 * 
 * These tests verify the business logic of authentication without UI dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        
        // Setup default mock returns
        coEvery { authRepository.isLoggedIn() } returns flowOf(false)
        coEvery { authRepository.getCurrentUser() } returns flowOf(null)
        
        authViewModel = AuthViewModel(authRepository)
    }

    @Test
    fun `initial state should be idle when user not logged in`() = runTest {
        // Given: Repository returns user is not logged in
        coEvery { authRepository.isLoggedIn() } returns flowOf(false)
        coEvery { authRepository.getCurrentUser() } returns flowOf(null)

        // When: ViewModel is initialized
        val viewModel = AuthViewModel(authRepository)

        // Then: State should be idle
        assertEquals(AuthState.Idle, viewModel.authState.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `initial state should be success when user is logged in`() = runTest {
        // Given: Repository returns user is logged in
        val userName = "John Doe"
        coEvery { authRepository.isLoggedIn() } returns flowOf(true)
        coEvery { authRepository.getCurrentUser() } returns flowOf(userName)

        // When: ViewModel is initialized
        val viewModel = AuthViewModel(authRepository)

        // Then: State should be success with user data
        val authState = viewModel.authState.value
        assertTrue(authState is AuthState.Success)
        assertEquals(userName, (authState as AuthState.Success).user.name)
    }

    @Test
    fun `handleGoogleSignInResult should handle successful authentication`() = runTest {
        // Given: Successful Google Sign-In task
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val idToken = "valid_id_token"
        val user = User("123", "456", "john@example.com", "John Doe", "2024-01-01", "2024-01-01")
        val authResult = AuthResult(token = "jwt_token", user = user)

        coEvery { authRepository.handleGoogleSignInResult(mockTask) } returns Result.success(idToken)
        coEvery { authRepository.authenticateWithGoogle(idToken) } returns Result.success(authResult)

        // When: Handling Google Sign-In result
        authViewModel.handleGoogleSignInResult(mockTask)

        // Then: State should be success
        val finalState = authViewModel.authState.value
        assertTrue(finalState is AuthState.Success)
        assertEquals(user, (finalState as AuthState.Success).user)
        assertFalse(authViewModel.isLoading.value)
        assertNull(authViewModel.errorMessage.value)
    }

    @Test
    fun `handleGoogleSignInResult should handle Google Sign-In failure`() = runTest {
        // Given: Failed Google Sign-In task
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val errorMessage = "Google Sign-In failed"

        coEvery { authRepository.handleGoogleSignInResult(mockTask) } returns Result.failure(Exception(errorMessage))

        // When: Handling failed Google Sign-In result
        authViewModel.handleGoogleSignInResult(mockTask)

        // Then: State should be error
        val finalState = authViewModel.authState.value
        assertTrue(finalState is AuthState.Error)
        assertEquals(errorMessage, (finalState as AuthState.Error).message)
        assertFalse(authViewModel.isLoading.value)
        assertEquals(errorMessage, authViewModel.errorMessage.value)
    }

    @Test
    fun `handleGoogleSignInResult should handle backend authentication failure`() = runTest {
        // Given: Successful Google Sign-In but failed backend authentication
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val idToken = "valid_id_token"
        val errorMessage = "Backend authentication failed"

        coEvery { authRepository.handleGoogleSignInResult(mockTask) } returns Result.success(idToken)
        coEvery { authRepository.authenticateWithGoogle(idToken) } returns Result.failure(Exception(errorMessage))

        // When: Handling Google Sign-In result with backend failure
        authViewModel.handleGoogleSignInResult(mockTask)

        // Then: State should be error
        val finalState = authViewModel.authState.value
        assertTrue(finalState is AuthState.Error)
        assertEquals(errorMessage, (finalState as AuthState.Error).message)
        assertFalse(authViewModel.isLoading.value)
        assertEquals(errorMessage, authViewModel.errorMessage.value)
    }

    @Test
    fun `signOut should handle successful sign out`() = runTest {
        // Given: Successful sign out
        coEvery { authRepository.signOut() } returns Result.success(Unit)

        // When: Signing out
        authViewModel.signOut()

        // Then: State should be idle
        assertEquals(AuthState.Idle, authViewModel.authState.value)
        assertFalse(authViewModel.isLoading.value)
        assertNull(authViewModel.errorMessage.value)
        
        // Verify repository signOut was called
        coVerify { authRepository.signOut() }
    }

    @Test
    fun `signOut should handle sign out failure`() = runTest {
        // Given: Failed sign out
        val errorMessage = "Sign out failed"
        coEvery { authRepository.signOut() } returns Result.failure(Exception(errorMessage))

        // When: Signing out
        authViewModel.signOut()

        // Then: Error message should be set
        assertFalse(authViewModel.isLoading.value)
        assertEquals(errorMessage, authViewModel.errorMessage.value)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given: ViewModel has an error message
        authViewModel.handleGoogleSignInResult(mockk())
        coEvery { authRepository.handleGoogleSignInResult(any()) } returns Result.failure(Exception("Error"))

        // When: Clearing error
        authViewModel.clearError()

        // Then: Error message should be null
        assertNull(authViewModel.errorMessage.value)
    }

    @Test
    fun `loading state should be managed correctly during authentication`() = runTest {
        // Given: Mock setup for authentication flow
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val idToken = "valid_id_token"
        val user = User("123", "456", "john@example.com", "John Doe", "2024-01-01", "2024-01-01")
        val authResult = AuthResult(token = "jwt_token", user = user)

        coEvery { authRepository.handleGoogleSignInResult(mockTask) } returns Result.success(idToken)
        coEvery { authRepository.authenticateWithGoogle(idToken) } returns Result.success(authResult)

        // When: Starting authentication
        authViewModel.handleGoogleSignInResult(mockTask)

        // Then: Loading state should be false after completion
        assertFalse(authViewModel.isLoading.value)
    }

    @Test
    fun `getGoogleSignInClient should delegate to repository`() {
        // When: Getting Google Sign-In client
        authViewModel.getGoogleSignInClient()

        // Then: Repository method should be called
        verify { authRepository.getGoogleSignInClient() }
    }
}
