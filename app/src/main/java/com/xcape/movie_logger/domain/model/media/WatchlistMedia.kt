package com.xcape.movie_logger.domain.model.media

import java.util.*

data class WatchlistMedia(
    val id: String = "",
    val addedOn: Date? = null,
    val dateReleased: String = "",
    val rating: Double = 0.0,
    val title: String = "",

    val mediaInfo: MediaInfo? = null,
)