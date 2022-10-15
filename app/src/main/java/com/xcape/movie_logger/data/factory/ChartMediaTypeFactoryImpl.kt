package com.xcape.movie_logger.data.factory

import android.view.LayoutInflater
import android.view.ViewGroup
import com.xcape.movie_logger.domain.factory.ChartMediaTypeFactory
import com.xcape.movie_logger.domain.model.media.PopularChartMedia
import com.xcape.movie_logger.domain.model.media.TopChartMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.trending.viewholders.PopularChartViewHolder
import com.xcape.movie_logger.presentation.trending.viewholders.TopChartViewHolder

class ChartMediaTypeFactoryImpl : ChartMediaTypeFactory {
    override fun getChartMediaType(topChartMedia: TopChartMedia): Int
        = ChartMediaType.TOP_CHART.ordinal

    override fun getChartMediaType(popularMedia: PopularChartMedia): Int
        = ChartMediaType.POPULAR_CHART.ordinal

    override fun getChartMediaViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) : BaseViewHolder<*> {
        return when (viewType) {
            ChartMediaType.TOP_CHART.ordinal ->
                TopChartViewHolder.create(inflater, parent)
            ChartMediaType.POPULAR_CHART.ordinal ->
                PopularChartViewHolder.create(inflater, parent)
            else -> throw IllegalArgumentException("Invalid view type!")
        }
    }
}