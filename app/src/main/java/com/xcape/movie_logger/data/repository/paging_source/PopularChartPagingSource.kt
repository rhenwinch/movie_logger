package com.xcape.movie_logger.data.repository.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xcape.movie_logger.domain.model.media.PopularChartMedia
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.domain.utils.Constants.PAGE_SIZE

// A paging source for Trending Fragment's popular lists
class PopularChartPagingSource (
    private val query: Resource<List<PopularChartMedia>>
) : PagingSource<List<PopularChartMedia>, PopularChartMedia>() {

    override fun getRefreshKey(state: PagingState<List<PopularChartMedia>, PopularChartMedia>): List<PopularChartMedia>? {
        return null
    }

    override suspend fun load(params: LoadParams<List<PopularChartMedia>>): LoadResult<List<PopularChartMedia>, PopularChartMedia> {
        return try {
            val currentQuery = params.key ?: query.data!!
            var nextQuery: List<PopularChartMedia>? = currentQuery.drop(PAGE_SIZE)

            if(nextQuery!!.isEmpty())
                nextQuery = null

            LoadResult.Page(
                data = currentQuery.take(PAGE_SIZE),
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