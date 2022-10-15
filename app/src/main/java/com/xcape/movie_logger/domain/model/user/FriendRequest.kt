package com.xcape.movie_logger.domain.model.user

import java.util.*

data class FriendRequest(
    val from: User? = null,
    val date: Date? = null
)
