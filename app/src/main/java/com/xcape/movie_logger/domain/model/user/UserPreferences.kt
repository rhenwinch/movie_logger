package com.xcape.movie_logger.domain.model.user

data class UserPreferences(
    val isFriendsPrivate: Boolean = false,
    val isWatchlistPrivate: Boolean = false,
    val isReviewsPrivate: Boolean = false,
)
