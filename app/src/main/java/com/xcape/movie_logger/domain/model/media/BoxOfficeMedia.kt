package com.xcape.movie_logger.domain.model.media

data class BoxOfficeMedia(
    val dateReleased: String,
    val id: String,
    val gallery: Gallery,
    val name: String,
    val rating: Double,
    val type: String,
    val year: String
)
