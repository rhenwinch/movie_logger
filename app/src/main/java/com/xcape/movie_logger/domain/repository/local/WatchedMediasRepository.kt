package com.xcape.movie_logger.domain.repository.local

import com.xcape.movie_logger.domain.model.media.WatchedMedia
import kotlinx.coroutines.flow.Flow

interface WatchedMediasRepository {
    suspend fun getAllWatchedMedias(): List<WatchedMedia>

    fun getLatestWatchedMedias(): Flow<List<WatchedMedia>>

    fun getOldestWatchedMedias(): Flow<List<WatchedMedia>>

    fun getMostPopularWatchedMedias(): Flow<List<WatchedMedia>>

    fun getLeastPopularWatchedMedias(): Flow<List<WatchedMedia>>

    fun getRecentlyReleasedWatchedMedias(): Flow<List<WatchedMedia>>

    fun getOldestReleasedWatchedMedias(): Flow<List<WatchedMedia>>

    fun getAtoZWatchedMedias(): Flow<List<WatchedMedia>>

    fun getZtoAWatchedMedias(): Flow<List<WatchedMedia>>

    suspend fun getWatchedMediaByMediaId(mediaId: String): WatchedMedia?

    suspend fun insertWatchedMedia(media: WatchedMedia)

    suspend fun deleteWatchedMediaById(mediaId: String)

    suspend fun deleteWatchedMedia(media: WatchedMedia)

    suspend fun deleteAll()

    suspend fun updateUserReviews()
}