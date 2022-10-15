package com.xcape.movie_logger.domain.model.media

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "watchlist")
data class WatchlistMedia(
    @PrimaryKey(autoGenerate = true) val uniqueId: Long = 0L,
    @ColumnInfo(name = "media_id") val id: String = "",
    @ColumnInfo(name = "added_on") val addedOn: Date? = null,
    @ColumnInfo(name = "date_released") val dateReleased: String = "",
    @ColumnInfo(name = "rating") val rating: Double = 0.0,
    @ColumnInfo(name = "title") val title: String = "",

    @ColumnInfo(name = "media_data") val mediaInfo: MediaInfo? = null,
)