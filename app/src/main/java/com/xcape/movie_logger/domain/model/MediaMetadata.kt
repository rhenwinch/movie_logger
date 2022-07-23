package com.xcape.movie_logger.domain.model

import com.xcape.movie_logger.domain.model.movie_data.Gallery

data class MediaMetadata(
    val dateReleased: String,
    val id: String,
    val gallery: Gallery,
    val name: String,
    val rating: Double,
    val type: String,
    val year: String
)