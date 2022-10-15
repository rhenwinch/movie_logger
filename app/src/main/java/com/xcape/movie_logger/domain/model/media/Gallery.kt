package com.xcape.movie_logger.domain.model.media

import java.io.Serializable

data class Gallery(
    val poster: String = "",
    val thumbnail: String? = null,
    val trailer: ImdbVideoPlayback? = null
) : Serializable