package com.xcape.movie_logger.data.repository.local

import com.xcape.movie_logger.data.local.dao.WatchedMediasDao
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.local.WatchedMediasRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WatchedMediasRepositoryImpl @Inject constructor(
    private val mediasDao: WatchedMediasDao,
    private val localUserRepository: LocalUserRepository
) : WatchedMediasRepository {
    override suspend fun getAllWatchedMedias(): List<WatchedMedia> {
       return mediasDao.getAllWatchedMedias()
    }

    override fun getLatestWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getLatestWatchedMedias()
    }

    override fun getOldestWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getOldestWatchedMedias()
    }

    override fun getMostPopularWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getMostPopularWatchedMedias()
    }

    override fun getLeastPopularWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getLeastPopularWatchedMedias()
    }

    override fun getRecentlyReleasedWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getRecentlyReleasedWatchedMedias()
    }

    override fun getOldestReleasedWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getOldestReleasedWatchedMedias()
    }

    override fun getAtoZWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getAtoZWatchedMedias()
    }

    override fun getZtoAWatchedMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getZtoAWatchedMedias()
    }

    override suspend fun getWatchedMediaByMediaId(mediaId: String): WatchedMedia? {
        return mediasDao.getWatchedMediaByMediaId(mediaId)
    }

    override suspend fun insertWatchedMedia(media: WatchedMedia) {
        mediasDao.insertWatchedMedia(media)
        updateUserReviews()
    }

    override suspend fun deleteWatchedMediaById(mediaId: String) {
        mediasDao.deleteWatchedMediaById(mediaId)
        updateUserReviews()
    }

    override suspend fun deleteWatchedMedia(media: WatchedMedia) {
        mediasDao.deleteWatchedMedia(media)
        updateUserReviews()
    }

    override suspend fun deleteAll() {
        mediasDao.deleteAllWatchedList()
        updateUserReviews()
    }

    override suspend fun updateUserReviews() {
        localUserRepository.updateReviews(mediasDao.getAllWatchedMedias())
    }
}