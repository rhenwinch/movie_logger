package com.xcape.movie_logger.data.local.dao

import androidx.room.*
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistMediasDao {
    @Query("SELECT * FROM watchlist")
    suspend fun getAllWatchlistMedias(): List<WatchlistMedia>

    @Query("SELECT * FROM watchlist ORDER BY added_on DESC")
    fun getLatestWatchlistMedias(): Flow<List<WatchlistMedia>>

    @Query("SELECT * FROM watchlist ORDER BY added_on ASC")
    fun getOldestWatchlistMedias(): Flow<List<WatchlistMedia>>

    @Query("SELECT * FROM watchlist ORDER BY rating DESC")
    fun getMostPopularWatchlistMedias(): Flow<List<WatchlistMedia>>

    @Query("SELECT * FROM watchlist ORDER BY rating ASC")
    fun getLeastPopularWatchlistMedias(): Flow<List<WatchlistMedia>>

    @Query("SELECT * FROM watchlist ORDER BY date_released DESC")
    fun getRecentlyReleasedWatchlistMedias(): Flow<List<WatchlistMedia>>

    @Query("SELECT * FROM watchlist ORDER BY date_released ASC")
    fun getOldestReleasedWatchlistMedias(): Flow<List<WatchlistMedia>>

    @Query("SELECT * FROM watchlist WHERE media_id = :mediaId")
    suspend fun getWatchlistMediaByMediaId(mediaId: String): WatchlistMedia?

    @Query("SELECT * FROM watchlist ORDER BY title DESC")
    fun getAtoZWatchlistMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watchlist ORDER BY title ASC")
    fun getZtoAWatchlistMedias(): Flow<List<WatchedMedia>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWatchlistMedia(media: WatchlistMedia)

    @Query("DELETE FROM watchlist WHERE media_id = :mediaId")
    suspend fun deleteWatchlistMediaById(mediaId: String)

    @Delete
    suspend fun deleteWatchlistMedia(media: WatchlistMedia)

    @Query("DELETE FROM watchlist")
    suspend fun deleteAllWatchlist()
}