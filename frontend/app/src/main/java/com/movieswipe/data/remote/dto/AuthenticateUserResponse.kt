package com.movieswipe.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthenticateUserResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: AuthResultDto?
)
