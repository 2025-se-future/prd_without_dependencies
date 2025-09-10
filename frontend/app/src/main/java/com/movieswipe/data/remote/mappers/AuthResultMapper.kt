package com.movieswipe.data.remote.mappers

import com.movieswipe.data.remote.dto.AuthResultDto
import com.movieswipe.utils.AuthResult

fun AuthResultDto.toDomain(): AuthResult {
    return AuthResult(
        token = token,
        user = user.toDomain()
    )
}
