package com.xcape.movie_logger.domain.model.user

import java.util.*

data class FriendRequest(
    val fromUserId: String? = null,
    val imageProfile: String? = null,
    val username: String? = null,
    val date: Date? = null
)
