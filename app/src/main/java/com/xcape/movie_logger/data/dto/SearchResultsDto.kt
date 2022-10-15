package com.xcape.movie_logger.data.dto

import com.xcape.movie_logger.domain.model.media.MediaInfo

data class SearchResultsDto(
    val totalPages: Int,
    val page: Int,
    val data: List<MediaInfo>?
)