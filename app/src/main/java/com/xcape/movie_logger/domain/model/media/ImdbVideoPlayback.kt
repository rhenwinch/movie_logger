package com.xcape.movie_logger.domain.model.media

import java.io.Serializable

data class ImdbVideoPlayback(
    val encodings: List<ImdbVideoEncodings> = listOf(),
    val preview: String = ""
) : Serializable

data class ImdbVideoEncodings(
    val definition: String = "",
    val playUrl: String = ""
) : Serializable
