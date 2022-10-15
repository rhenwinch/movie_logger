package com.xcape.movie_logger.domain.repository.local

import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    suspend fun getAllWatchlistMedias(): List<WatchlistMedia>

    fun getLatestWatchlistMedias(): Flow<List<WatchlistMedia>>

    fun getOldestWatchlistMedias(): Flow<List<WatchlistMedia>>

    fun getMostPopularWatchlistMedias(): Flow<List<WatchlistMedia>>

    fun getLeastPopularWatchlistMedias(): Flow<List<WatchlistMedia>>

    fun getRecentlyReleasedWatchlistMedias(): Flow<List<WatchlistMedia>>

    fun getOldestReleasedWatchlistMedias(): Flow<List<WatchlistMedia>>

    fun getAtoZWatchlistMedias(): Flow<List<WatchedMedia>>

    fun getZtoAWatchlistMedias(): Flow<List<WatchedMedia>>

    suspend fun getWatchlistMediaByMediaId(mediaId: String): WatchlistMedia?

    suspend fun insertWatchlistMedia(media: WatchlistMedia)

    suspend fun deleteWatchlistMediaById(mediaId: String)

    suspend fun deleteWatchlistMedia(media: WatchlistMedia)

    suspend fun deleteAll()

    suspend fun updateUserWatchlist()
}