package com.xcape.movie_logger.domain.model

data class SuggestedMovie(
    val i: SuggestedMovieImage? = null,
    val id: String? = null,
    val l: String? = null
)

data class SuggestedMovieImage(val imageUrl: String)
