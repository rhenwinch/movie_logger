package com.xcape.movie_logger.domain.repository.remote

import com.google.firebase.firestore.CollectionReference
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getLatestWatchlistMedias(): Flow<List<WatchlistMedia>>
    suspend fun getWatchlistMediaByMediaId(mediaId: String): WatchlistMedia?
    fun insertWatchlistMedia(media: WatchlistMedia)
    fun deleteWatchlistMediaById(mediaId: String)
    fun deleteAll()
    fun updateWatchlist(): CollectionReference
}