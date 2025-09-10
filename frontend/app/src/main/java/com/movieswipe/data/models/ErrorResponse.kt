package com.movieswipe.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "errors")
    val errors: List<FieldError>?
)

@JsonClass(generateAdapter = true)
data class FieldError(
    @Json(name = "field")
    val field: String,
    @Json(name = "message")
    val message: String
)
