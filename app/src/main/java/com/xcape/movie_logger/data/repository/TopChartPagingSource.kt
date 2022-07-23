package com.xcape.movie_logger.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.domain.repository.MovieRepository
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.utils.Constants.PAGE_SIZE
import javax.inject.Inject

class TopChartPagingSource @Inject constructor(
    private val query: Resource<List<String>>,
    private val repository: MovieRepository
) : PagingSource<List<String>, MediaMetadata>() {
    override fun getRefreshKey(state: PagingState<List<String>, MediaMetadata>): List<String>? {
        return null
    }

    override suspend fun load(params: LoadParams<List<String>>): LoadResult<List<String>, MediaMetadata> {
        return try {
            val currentQuery = params.key ?: query.data!!
            val batchedResponse = repository.getBatchMovies(currentQuery.take(PAGE_SIZE.toInt()))
            val nextQuery = currentQuery.drop(PAGE_SIZE.toInt())

            LoadResult.Page(
                data = batchedResponse.data!!,
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