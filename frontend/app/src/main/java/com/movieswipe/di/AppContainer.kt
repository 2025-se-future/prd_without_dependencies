package com.movieswipe.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.movieswipe.data.local.PreferencesManager
import com.movieswipe.data.local.TokenManager
import com.movieswipe.data.remote.AuthApi
import com.movieswipe.data.remote.GoogleSignInHelper
import com.movieswipe.data.repositories.users.AuthRepository
import com.movieswipe.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "movie_swipe_preferences")

class AppContainer(private val context: Context) {
    
    // DataStore
    val dataStore: DataStore<Preferences> = context.dataStore
    
    // Moshi
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // OkHttp Client
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    // Retrofit
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    
    // APIs
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    
    // Local storage
    val tokenManager = TokenManager(dataStore)
    val preferencesManager = PreferencesManager(dataStore)
    
    // Helpers
    val googleSignInHelper = GoogleSignInHelper(context)
    
    // Repositories
    val authRepository = AuthRepository(
        authApi = authApi,
        tokenManager = tokenManager,
        preferencesManager = preferencesManager,
        googleSignInHelper = googleSignInHelper,
        moshi = moshi
    )
}
