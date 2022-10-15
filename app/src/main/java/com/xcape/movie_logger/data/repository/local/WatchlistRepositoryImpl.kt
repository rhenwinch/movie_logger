package com.xcape.movie_logger.data.repository.local

import com.xcape.movie_logger.data.local.dao.WatchlistMediasDao
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.repository.local.WatchlistRepository
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WatchlistRepositoryImpl @Inject constructor(
    private val mediasDao: WatchlistMediasDao,
    private val localUserRepository: LocalUserRepository
) : WatchlistRepository {
    override suspend fun getAllWatchlistMedias(): List<WatchlistMedia> {
        val user = localUserRepository.getUser()
        return user.
    }

    override fun getLatestWatchlistMedias(): Flow<List<WatchlistMedia>> {
        return mediasDao.getLatestWatchlistMedias()
    }

    override fun getOldestWatchlistMedias(): Flow<List<WatchlistMedia>> {
        return mediasDao.getOldestWatchlistMedias()
    }

    override fun getMostPopularWatchlistMedias(): Flow<List<WatchlistMedia>> {
        return mediasDao.getMostPopularWatchlistMedias()
    }

    override fun getLeastPopularWatchlistMedias(): Flow<List<WatchlistMedia>> {
        return mediasDao.getLeastPopularWatchlistMedias()
    }

    override fun getRecentlyReleasedWatchlistMedias(): Flow<List<WatchlistMedia>> {
        return mediasDao.getRecentlyReleasedWatchlistMedias()
    }

    override fun getOldestReleasedWatchlistMedias(): Flow<List<WatchlistMedia>> {
        return mediasDao.getOldestReleasedWatchlistMedias()
    }

    override fun getAtoZWatchlistMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getAtoZWatchlistMedias()
    }

    override fun getZtoAWatchlistMedias(): Flow<List<WatchedMedia>> {
        return mediasDao.getZtoAWatchlistMedias()
    }

    override suspend fun getWatchlistMediaByMediaId(mediaId: String): WatchlistMedia? {
        return mediasDao.getWatchlistMediaByMediaId(mediaId)
    }

    override suspend fun insertWatchlistMedia(media: WatchlistMedia) {
        mediasDao.insertWatchlistMedia(media)
        updateUserWatchlist()
    }

    override suspend fun deleteWatchlistMediaById(mediaId: String) {
        mediasDao.deleteWatchlistMediaById(mediaId)
        updateUserWatchlist()
    }

    override suspend fun deleteWatchlistMedia(media: WatchlistMedia) {
        mediasDao.deleteWatchlistMedia(media)
        updateUserWatchlist()
    }

    override suspend fun deleteAll() {
        mediasDao.deleteAllWatchlist()
        updateUserWatchlist()
    }

    override suspend fun updateUserWatchlist() {
        localUserRepository.updateWatchlist(mediasDao.getAllWatchlistMedias())
    }
}