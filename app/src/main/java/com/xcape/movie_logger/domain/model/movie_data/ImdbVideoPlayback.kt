package com.xcape.movie_logger.domain.model.movie_data

data class ImdbVideoPlayback(
    val encodings: List<ImdbVideoEncodings>,
    val preview: String
)

data class ImdbVideoEncodings(
    val definition: String,
    val playUrl: String
)
