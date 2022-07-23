package com.xcape.movie_logger.presentation.movie_details

import com.xcape.movie_logger.domain.model.Media

data class MovieUIState(
    var mediaData: Media? = null,
    var isLoading: Boolean = true,
    var errorMessage: String? = null
)
