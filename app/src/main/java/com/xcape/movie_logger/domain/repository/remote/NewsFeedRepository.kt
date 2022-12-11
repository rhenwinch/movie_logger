package com.xcape.movie_logger.domain.repository.remote

import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface NewsFeedRepository {
    fun getLatestPosts(): Flow<WatchedMedia>
}