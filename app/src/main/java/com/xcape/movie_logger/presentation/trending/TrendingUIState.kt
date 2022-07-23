package com.xcape.movie_logger.presentation.trending

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

data class TrendingUIState<T : Any>(
    val type: String,
    val flowPagingData: Flow<PagingData<T>>? = null,
    val flowListData: Flow<List<T>>? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)
