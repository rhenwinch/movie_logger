package com.xcape.movie_logger.domain.model.user

import java.util.*

data class Notification(
    val from: User? = null,
    val message: String = "",
    val mediaId: String = "",
    val timestamp: Date? = null
)