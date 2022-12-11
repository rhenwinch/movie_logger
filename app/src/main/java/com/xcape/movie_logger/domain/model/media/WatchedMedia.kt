package com.xcape.movie_logger.domain.model.media

import java.util.*

data class WatchedMedia(
    val id: String = "",
    val ownerId: String = "",
    val addedOn: Date? = null,
    val dateReleased: String = "",
    val comments: String? = null,
    val rating: Double = 0.0,
    val title: String = "",
    val likes: MutableList<String> = mutableListOf(),
    val userJournalPhoto: String? = null,
    val friendTags: List<String> = listOf(),

    val mediaInfo: MediaInfo? = null,
)