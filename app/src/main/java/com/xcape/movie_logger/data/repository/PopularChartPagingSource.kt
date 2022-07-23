package com.xcape.movie_logger.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xcape.movie_logger.domain.model.PopularChart
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.utils.Constants.PAGE_SIZE

// A paging source for Trending Fragment's popular lists
class PopularChartPagingSource (
    private val query: Resource<List<PopularChart>>
) : PagingSource<List<PopularChart>, PopularChart>() {

    override fun getRefreshKey(state: PagingState<List<PopularChart>, PopularChart>): List<PopularChart>? {
        return null
    }

    override suspend fun load(params: LoadParams<List<PopularChart>>): LoadResult<List<PopularChart>, PopularChart> {
        return try {
            val currentQuery = params.key ?: query.data!!
            val nextQuery = currentQuery.drop(PAGE_SIZE.toInt())

            LoadResult.Page(
                data = currentQuery.take(PAGE_SIZE.toInt()),
                prevKey = null,
                nextKey = nextQuery
            )
        }
        catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}