package com.movieswipe.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.movieswipe.utils.Constants
class GoogleSignInHelper(
    private val context: Context
) {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInClient(): GoogleSignInClient = googleSignInClient

    fun handleSignInResult(task: Task<GoogleSignInAccount>): Result<String> {
        return try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                Result.success(idToken)
            } else {
                Result.failure(Exception("Failed to get ID token"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            googleSignInClient.signOut()
        } catch (e: Exception) {
            // Handle sign out error if needed
        }
    }

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
}
