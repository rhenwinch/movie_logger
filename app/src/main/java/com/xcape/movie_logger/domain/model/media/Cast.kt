package com.xcape.movie_logger.domain.model.media

import java.io.Serializable

data class Cast(
    val characters: List<String> = listOf(),
    val name: String = ""
) : Serializable