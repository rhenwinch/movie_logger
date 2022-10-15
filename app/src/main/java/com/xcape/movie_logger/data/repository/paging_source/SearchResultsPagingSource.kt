package com.xcape.movie_logger.data.repository.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository

const val PAGE_LIMIT = 5

class SearchResultsPagingSource (
    private val repository: MovieRemoteRepository,
    private val mediaKeyword: String,
    private val recommendedMediasOnly: Boolean
) : PagingSource<Int, MediaInfo>() {

    override fun getRefreshKey(state: PagingState<Int, MediaInfo>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaInfo> {
        val currentPage = params.key ?: 1
        val batchedResponse = repository.searchMedia(mediaKeyword, currentPage, (params.loadSize / 3))

        if(batchedResponse.data == null) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null,
            )
        }

        val data: List<MediaInfo> = batchedResponse.data.data ?: emptyList()
        val nextPage =
            if(batchedResponse.data.totalPages == currentPage || recommendedMediasOnly || currentPage == PAGE_LIMIT) {
                null
            }
            else {
                currentPage + 1
            }

        return LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = nextPage,
        )
    }
}