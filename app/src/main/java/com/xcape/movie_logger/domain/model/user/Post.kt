package com.xcape.movie_logger.domain.model.user

import com.xcape.movie_logger.domain.model.media.WatchedMedia

enum class WatchStatus {
    WATCHED,
    WATCHLIST,
    NOT_WATCHED
}

data class Post(
    val isLiked: Boolean = false,
    val watchStatus: WatchStatus = WatchStatus.NOT_WATCHED,
    val post: WatchedMedia? = null
)