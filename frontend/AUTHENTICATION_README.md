# MovieSwipe Frontend - Authentication Feature Implementation

## Overview

This document provides a comprehensive overview of the implemented **"Manage Authentication"** feature for the MovieSwipe Android application. The implementation follows clean architecture principles, MVVM pattern, and Material Design 3 guidelines.

## Feature Description

The authentication feature enables users to:

- Sign in using Google OAuth authentication
- Automatically register new users upon first sign-in
- View their profile information
- Sign out from the application
- Maintain authentication state across app sessions

## Architecture

### Clean Architecture Layers

The implementation follows a three-layer architecture:

1. **Presentation Layer (UI)**

   - `LoginScreen.kt` - Google Sign-In interface
   - `ProfileScreen.kt` - User profile and sign-out functionality
   - `AuthViewModel.kt` - State management and business logic

2. **Domain Layer (Data)**

   - `AuthRepository.kt` - Data access abstraction
   - `TokenManager.kt` - Secure token storage
   - `PreferencesManager.kt` - User preferences storage

3. **Data Layer (Remote/Local)**
   - `AuthApi.kt` - Backend API communication
   - `GoogleSignInHelper.kt` - Google Sign-In integration
   - DataStore for persistent storage

### Key Components

#### 1. Data Models

```kotlin
// User representation from backend
data class User(
    val id: String,
    val googleId: String,
    val email: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String
)

// Authentication request to backend
data class AuthRequest(val idToken: String)

// Authentication response from backend
data class AuthResponse(
    val message: String,
    val data: AuthResult?
)

// UI state management
sealed class AuthState {
    object Loading : AuthState()
    object Idle : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

#### 2. Repository Pattern

```kotlin
@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager,
    private val googleSignInHelper: GoogleSignInHelper,
    private val moshi: Moshi
) {
    // Authentication methods
    suspend fun authenticateWithGoogle(idToken: String): Result<AuthResult>
    suspend fun signOut(): Result<Unit>
    fun isLoggedIn(): Flow<Boolean>
    // ... other methods
}
```

#### 3. ViewModel (MVVM)

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>)
    fun signOut()
    // ... other methods
}
```

#### 4. Secure Storage

```kotlin
@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun saveToken(token: String)
    suspend fun setLoggedIn(isLoggedIn: Boolean)
    fun getToken(): Flow<String?>
    fun isLoggedIn(): Flow<Boolean>
    // ... other methods
}
```

## Implementation Details

### Google Sign-In Integration

1. **Configuration**: Add Google Web Client ID in `Constants.kt`
2. **Helper Class**: `GoogleSignInHelper` manages Google Sign-In client
3. **Token Exchange**: ID token from Google is sent to backend for verification
4. **JWT Storage**: Backend JWT token stored securely using DataStore

### Navigation Flow

```
App Launch → Check Auth State → Login Screen (if not authenticated)
                             ↓
Google Sign-In → Backend Authentication → Groups Screen
                                        ↓
Profile Screen ← Navigation Menu ← Groups Screen
     ↓
Sign Out → Clear Storage → Login Screen
```

### State Management

- **StateFlow** for reactive UI updates
- **Loading states** for better UX
- **Error handling** with user-friendly messages
- **Persistent authentication** across app restarts

### Security Features

- JWT tokens stored in encrypted DataStore
- Automatic token management
- Secure sign-out process
- Input validation and sanitization

## File Structure

```
src/main/java/com/movieswipe/
├── data/
│   ├── local/
│   │   ├── TokenManager.kt
│   │   └── PreferencesManager.kt
│   ├── models/
│   │   ├── User.kt
│   │   ├── AuthRequest.kt
│   │   ├── AuthResponse.kt
│   │   ├── AuthState.kt
│   │   └── ErrorResponse.kt
│   ├── remote/
│   │   ├── AuthApi.kt
│   │   └── GoogleSignInHelper.kt
│   └── repositories/users/
│       └── AuthRepository.kt
├── di/
│   ├── NetworkModule.kt
│   └── DatabaseModule.kt
├── ui/
│   ├── components/
│   │   └── ErrorMessage.kt
│   ├── navigation/
│   │   ├── AppNavigation.kt
│   │   └── NavigationRoutes.kt
│   ├── screens/users/
│   │   ├── LoginScreen.kt
│   │   └── ProfileScreen.kt
│   └── viewmodels/users/
│       └── AuthViewModel.kt
├── utils/
│   └── Constants.kt
└── MovieSwipeApplication.kt
```

## Testing

### Comprehensive Test Coverage

1. **Unit Tests**

   - `AuthViewModelTest.kt` - ViewModel business logic
   - `AuthRepositoryTest.kt` - Repository data operations

2. **Integration Tests**

   - `AuthenticationE2ETest.kt` - End-to-end user flows
   - UI component testing
   - Navigation testing

3. **Test Scenarios**
   - Successful authentication flow
   - Error handling (network, invalid tokens)
   - State management verification
   - Sign-out functionality
   - Persistent authentication

### Test Libraries Used

- **MockK** for mocking dependencies
- **Kotlinx Coroutines Test** for async testing
- **Compose UI Test** for UI testing
- **Hilt Testing** for dependency injection testing

## Setup Instructions

### 1. Google Sign-In Configuration

1. Create a project in Google Cloud Console
2. Enable Google Sign-In API
3. Create OAuth 2.0 credentials
4. Add the Web Client ID to `Constants.kt`:
   ```kotlin
   const val GOOGLE_WEB_CLIENT_ID = "your_actual_client_id_here"
   ```

### 2. Backend Configuration

Update the base URL in `Constants.kt`:

```kotlin
const val BASE_URL = "http://your_backend_url/api/"
```

### 3. Dependencies

All required dependencies are configured in `build.gradle.kts`:

- Hilt for dependency injection
- Retrofit for networking
- DataStore for storage
- Google Play Services Auth
- Compose UI libraries

## Usage Examples

### Basic Authentication Flow

```kotlin
// In your activity or screen
val authViewModel: AuthViewModel = hiltViewModel()
val authState by authViewModel.authState.collectAsState()

when (authState) {
    is AuthState.Success -> {
        // Navigate to main app
        navController.navigate("groups")
    }
    is AuthState.Error -> {
        // Show error message
        ErrorMessage(authState.message)
    }
    AuthState.Loading -> {
        // Show loading indicator
        CircularProgressIndicator()
    }
    AuthState.Idle -> {
        // Show login screen
        LoginScreen()
    }
}
```

### Checking Authentication Status

```kotlin
// Check if user is authenticated
val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

if (isLoggedIn) {
    // User is authenticated
} else {
    // Show login screen
}
```

## Best Practices Implemented

1. **Clean Architecture** - Separation of concerns
2. **MVVM Pattern** - Reactive UI with state management
3. **Dependency Injection** - Testable and maintainable code
4. **Material Design 3** - Modern UI/UX
5. **Error Handling** - Comprehensive error management
6. **Security** - Secure token storage and management
7. **Testing** - Comprehensive test coverage
8. **Code Quality** - Following Android/Kotlin best practices

## Future Enhancements

1. **Biometric Authentication** - Fingerprint/Face ID support
2. **Offline Support** - Cached authentication state
3. **Multi-factor Authentication** - Additional security layer
4. **Social Login** - Support for other providers (Facebook, Twitter)
5. **Session Management** - Token refresh and expiration handling

## Troubleshooting

### Common Issues

1. **Google Sign-In Not Working**

   - Verify Google Web Client ID is correct
   - Check SHA-1 certificate fingerprint in Google Console
   - Ensure Google Play Services are installed

2. **Backend Authentication Failing**

   - Verify backend URL is correct
   - Check network connectivity
   - Validate backend API is running

3. **Storage Issues**
   - Clear app data if DataStore corruption occurs
   - Verify permissions for local storage

### Debug Tips

1. Enable logging interceptor for network calls
2. Use Android Studio debugger for state inspection
3. Monitor DataStore values using Device File Explorer
4. Check Logcat for authentication errors

## API Compatibility

- **Backend API**: Compatible with `/auth` endpoint as documented in `backend/docs/auth.yml`
- **Google Sign-In**: Compatible with Google Play Services Auth v20.7.0+
- **Android**: Minimum SDK 33 (Android Tiramisu)

## Conclusion

The authentication feature implementation provides a robust, secure, and user-friendly authentication system for the MovieSwipe application. It follows modern Android development practices and provides a solid foundation for future feature development.

The implementation is ready for production use and includes comprehensive testing to ensure reliability and maintainability.
