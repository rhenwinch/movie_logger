package com.xcape.movie_logger.domain.factory

import android.view.LayoutInflater
import android.view.ViewGroup
import com.xcape.movie_logger.domain.model.media.PopularChartMedia
import com.xcape.movie_logger.domain.model.media.TopChartMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder

// Media Type Object Provider
interface ChartMediaTypeFactory {
    fun getChartMediaType(topChartMedia: TopChartMedia) : Int
    fun getChartMediaType(popularMedia: PopularChartMedia) : Int
    fun getChartMediaViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) : BaseViewHolder<*>
}