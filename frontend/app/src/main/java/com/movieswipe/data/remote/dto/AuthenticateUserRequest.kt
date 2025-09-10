package com.movieswipe.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthenticateUserRequest(
    @SerializedName("idToken")
    val idToken: String
)
