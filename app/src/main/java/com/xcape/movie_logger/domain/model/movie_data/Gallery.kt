package com.xcape.movie_logger.domain.model.movie_data

data class Gallery(
    val poster: String,
    val thumbnail: String,
    val trailer: ImdbVideoPlayback
)