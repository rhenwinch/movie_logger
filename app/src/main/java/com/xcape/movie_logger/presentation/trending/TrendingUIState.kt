package com.xcape.movie_logger.presentation.trending

import androidx.paging.PagingData
import com.xcape.movie_logger.domain.model.media.BoxOfficeMedia
import com.xcape.movie_logger.domain.model.media.PopularChartMedia
import com.xcape.movie_logger.domain.model.media.TopChartMedia
import kotlinx.coroutines.flow.Flow

sealed class TrendingUIAction {
    data class Refresh(val triggerRefresh: Boolean): TrendingUIAction()
}

data class TrendingUIState(
    val popularChartData: MutableMap<String, Flow<PagingData<PopularChartMedia>>> = mutableMapOf(),
    val topChartData: MutableMap<String, Flow<PagingData<TopChartMedia>>> = mutableMapOf(),
    val boxOfficeData: Flow<List<BoxOfficeMedia>>? = null,
    val isRefreshing: Boolean = false,
    val hasFinishedFetching: Boolean = false,
    val hasErrors: Boolean = false,
)
