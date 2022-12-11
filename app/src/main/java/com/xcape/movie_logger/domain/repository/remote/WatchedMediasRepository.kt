package com.xcape.movie_logger.domain.repository.remote

import com.google.firebase.firestore.CollectionReference
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import kotlinx.coroutines.flow.Flow

interface WatchedMediasRepository {
    fun getLatestWatchedMedias(): Flow<List<WatchedMedia>>
    suspend fun getWatchedMediaByMediaId(mediaId: String, userId: String? = null): WatchedMedia?
    fun insertWatchedMedia(media: WatchedMedia)
    fun deleteWatchedMediaById(mediaId: String)
    fun updateReviews(): CollectionReference
}