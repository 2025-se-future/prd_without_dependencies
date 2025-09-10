package com.movieswipe.data.remote.mappers

import com.movieswipe.data.remote.dto.UserDto
import com.movieswipe.utils.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        googleId = googleId,
        email = email,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
