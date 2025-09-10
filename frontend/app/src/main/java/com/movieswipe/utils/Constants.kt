package com.movieswipe.utils

object Constants {
    // API
    const val BASE_URL = "http://10.0.2.2:3000/api/"
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // Authentication
    const val TOKEN_KEY = "jwt_token"
    const val USER_ID_KEY = "user_id"
    const val IS_LOGGED_IN_KEY = "is_logged_in"
    
    // Google Sign-In
    const val GOOGLE_WEB_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID" // Replace with actual client ID
    
    // Navigation
    const val LOGIN_ROUTE = "login"
    const val PROFILE_ROUTE = "profile"
    const val GROUPS_ROUTE = "groups"
}
