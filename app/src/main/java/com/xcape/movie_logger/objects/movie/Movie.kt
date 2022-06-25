package com.xcape.movie_logger.objects.movie

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "movie_database", indices = [Index(value = ["movie_title", "movie_token_id"], unique = true)])
data class Movie(
    @PrimaryKey(autoGenerate = true)
    var movieKey: Long = 0L,

    @ColumnInfo(name = "movie_token_id")
    var movieTokenID: String? = null,

    @ColumnInfo(name = "watched_on")
    var dateWatched: String? = null,

    @ColumnInfo(name = "movie_title")
    var movieTitle: String? = null,

    @ColumnInfo(name = "movie_image_poster")
    var movieImagePoster: String? = null,

    @ColumnInfo(name = "movie_image_thumb")
    var movieImageThumb: String? = null,

    @ColumnInfo(name = "movie_description")
    var movieDesc: String? = null,

    @ColumnInfo(name = "movie_rating")
    var movieRating: Float = 0.0F,

    @ColumnInfo(name = "movie_genre")
    var movieGenre: String? = null,

    @ColumnInfo(name = "movie_date_released")
    var movieDateReleased: String? = null,

    @ColumnInfo(name = "movie_cast")
    var movieCast: String? = null,

    @ColumnInfo(name = "movie_directors")
    var movieDirectors: String? = null,

    @ColumnInfo(name = "movie_duration")
    var movieDuration: String? = null
)
