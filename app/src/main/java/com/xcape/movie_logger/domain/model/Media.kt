package com.xcape.movie_logger.domain.model

import com.xcape.movie_logger.domain.model.movie_data.Cast
import com.xcape.movie_logger.domain.model.movie_data.Gallery

data class Media(
    val casts: List<Cast>,
    val dateReleased: String,
    val directors: List<String>,
    val duration: String,
    val gallery: Gallery,
    val genres: List<String>,
    val id: String,
    val plotLong: String,
    val plotShort: String,
    val rating: Double,
    val title: String,
    val type: String,
    val writers: List<String>,
    val year: String
)