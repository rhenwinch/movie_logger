package com.xcape.movie_logger.data.repository.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xcape.movie_logger.data.dto.toTopChart
import com.xcape.movie_logger.domain.model.media.TopChartMedia
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.domain.utils.Constants.PAGE_SIZE

class TopChartPagingSource (
    private val query: Resource<List<String>>,
    private val repository: MovieRemoteRepository
) : PagingSource<List<String>, TopChartMedia>() {
    override fun getRefreshKey(state: PagingState<List<String>, TopChartMedia>): List<String>? {
        return null
    }

    override suspend fun load(params: LoadParams<List<String>>): LoadResult<List<String>, TopChartMedia> {
        return try {
            val currentQuery = params.key ?: query.data!!
            val batchedResponse = repository.getBatchMedias(currentQuery.take(PAGE_SIZE))
            var nextQuery: List<String>? = currentQuery.drop(PAGE_SIZE)

            if(nextQuery!!.isEmpty())
                nextQuery = null

            val data: List<TopChartMedia> =
                if(batchedResponse.data == null)
                    emptyList()
                else
                    batchedResponse.data.map { it.toTopChart() }

            LoadResult.Page(
                data = data,
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