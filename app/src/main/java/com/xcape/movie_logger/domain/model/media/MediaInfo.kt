package com.xcape.movie_logger.domain.model.media

import java.io.Serializable

data class MediaInfo(
    val casts: List<Cast> = listOf(),
    val certificate: String? = null,
    val dateReleased: String = "",
    val directors: List<String> = listOf(),
    val duration: String = "",
    val gallery: Gallery = Gallery(),
    val genres: List<String> = listOf(),
    val id: String = "",
    val plotLong: String = "",
    val plotShort: String = "",
    val rating: Double = 0.0,
    val title: String = "",
    val type: String = "",
    val writers: List<String> = listOf(),
    val year: String = ""
) : Serializable