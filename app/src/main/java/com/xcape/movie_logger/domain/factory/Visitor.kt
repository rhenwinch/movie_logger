package com.xcape.movie_logger.domain.factory

interface Visitor {
    fun getMediaType(mediaFactory: ChartMediaTypeFactory) : Int
}