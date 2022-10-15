package com.xcape.movie_logger.domain.model.media

import com.xcape.movie_logger.domain.factory.ChartMediaTypeFactory
import com.xcape.movie_logger.domain.model.base.BaseChartMedia

data class PopularChartMedia(
    val id: String,
    val image: String,
    val name: String,
    val rank: Int,
    val rankChange: Int,
    val year: Int
) : BaseChartMedia {
    override fun getMediaType(mediaFactory: ChartMediaTypeFactory): Int
            = mediaFactory.getChartMediaType(this)
}