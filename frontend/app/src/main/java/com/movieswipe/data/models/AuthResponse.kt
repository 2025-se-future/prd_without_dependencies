package com.movieswipe.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "data")
    val data: AuthResult?
)

@JsonClass(generateAdapter = true)
data class AuthResult(
    @Json(name = "token")
    val token: String,
    @Json(name = "user")
    val user: User
)
