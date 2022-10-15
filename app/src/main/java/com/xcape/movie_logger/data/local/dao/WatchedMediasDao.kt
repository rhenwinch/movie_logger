package com.xcape.movie_logger.data.local.dao

import androidx.room.*
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedMediasDao {
    @Query("SELECT * FROM watched_list")
    suspend fun getAllWatchedMedias(): List<WatchedMedia>

    @Query("SELECT * FROM watched_list ORDER BY added_on DESC")
    fun getLatestWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list ORDER BY added_on ASC")
    fun getOldestWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list ORDER BY rating DESC")
    fun getMostPopularWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list ORDER BY rating ASC")
    fun getLeastPopularWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list ORDER BY date_released DESC")
    fun getRecentlyReleasedWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list ORDER BY date_released ASC")
    fun getOldestReleasedWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list ORDER BY title DESC")
    fun getAtoZWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list ORDER BY title ASC")
    fun getZtoAWatchedMedias(): Flow<List<WatchedMedia>>

    @Query("SELECT * FROM watched_list WHERE media_id = :mediaId")
    suspend fun getWatchedMediaByMediaId(mediaId: String): WatchedMedia?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWatchedMedia(media: WatchedMedia)

    @Query("DELETE FROM watched_list WHERE media_id = :mediaId")
    suspend fun deleteWatchedMediaById(mediaId: String)

    @Delete
    suspend fun deleteWatchedMedia(media: WatchedMedia)

    @Query("DELETE FROM watched_list")
    suspend fun deleteAllWatchedList()
}