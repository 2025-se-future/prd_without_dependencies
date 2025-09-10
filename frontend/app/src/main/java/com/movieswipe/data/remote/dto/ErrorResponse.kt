package com.movieswipe.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ValidationError(
    @SerializedName("field")
    val field: String,
    
    @SerializedName("message")
    val message: String
)

data class ErrorResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("errors")
    val errors: List<ValidationError>? = null
)
