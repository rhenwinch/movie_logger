package com.xcape.movie_logger.domain.model.media

import com.xcape.movie_logger.domain.factory.ChartMediaTypeFactory
import com.xcape.movie_logger.domain.model.base.BaseChartMedia

data class TopChartMedia(
    val dateReleased: String,
    val id: String,
    val gallery: Gallery,
    val name: String,
    val rating: Double,
    val type: String,
    val year: String
) : BaseChartMedia {
    override fun getMediaType(mediaFactory: ChartMediaTypeFactory): Int
        = mediaFactory.getChartMediaType(this)
}
