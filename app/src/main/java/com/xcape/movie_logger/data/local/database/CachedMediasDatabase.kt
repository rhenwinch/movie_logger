package com.xcape.movie_logger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xcape.movie_logger.data.local.dao.WatchedMediasDao
import com.xcape.movie_logger.data.local.dao.WatchlistMediasDao
import com.xcape.movie_logger.data.local.type_converter.MediaConverters
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia

@Database(
    entities = [WatchlistMedia::class, WatchedMedia::class],
    version = 1
)
@TypeConverters(MediaConverters::class)
abstract class CachedMediasDatabase: RoomDatabase() {
    abstract val watchlistDao: WatchlistMediasDao
    abstract val watchedListDao: WatchedMediasDao

    companion object {
        const val CACHED_MEDIAS_DATABASE = "cached_medias_database"
    }
}