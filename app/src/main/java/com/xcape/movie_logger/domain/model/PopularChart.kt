package com.xcape.movie_logger.domain.model

data class PopularChart(
    val id: String,
    val image: String,
    val name: String,
    val rank: Int,
    val rankChange: Int,
    val year: Int
)