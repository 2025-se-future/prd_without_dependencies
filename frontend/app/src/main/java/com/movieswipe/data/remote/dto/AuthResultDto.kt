package com.movieswipe.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResultDto(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("user")
    val user: UserDto
)
