package com.xcape.movie_logger.data.dto

import com.xcape.movie_logger.domain.model.media.BoxOfficeMedia
import com.xcape.movie_logger.domain.model.media.TopChartMedia
import com.xcape.movie_logger.domain.model.media.Gallery

data class MediaMetadata(
    val dateReleased: String,
    val id: String,
    val gallery: Gallery,
    val name: String,
    val rating: Double,
    val type: String,
    val year: String
)

fun MediaMetadata.toBoxOffice(): BoxOfficeMedia {
    return BoxOfficeMedia(
        dateReleased = dateReleased,
        id = id,
        gallery = gallery,
        name = name,
        rating = rating,
        type = type,
        year = year
    )
}

fun MediaMetadata.toTopChart(): TopChartMedia {
    return TopChartMedia(
        dateReleased = dateReleased,
        id = id,
        gallery = gallery,
        name = name,
        rating = rating,
        type = type,
        year = year
    )
}