package com.xcape.movie_logger.domain.repository.remote

import androidx.paging.PagingData
import com.xcape.movie_logger.data.dto.MediaMetadata
import com.xcape.movie_logger.data.dto.SearchResultsDto
import com.xcape.movie_logger.domain.model.auth.Credentials
import com.xcape.movie_logger.domain.model.media.*
import com.xcape.movie_logger.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MovieRemoteRepository {
    suspend fun getCredentials() : Resource<Credentials>

    suspend fun getBoxOffice() : Resource<List<BoxOfficeMedia>>

    suspend fun getPopularChartStream(type: String) : Flow<PagingData<PopularChartMedia>>

    fun getSearchResultsStream(
        mediaKeyword: String,
        recommendedMediasOnly: Boolean
    ) : Flow<PagingData<MediaInfo>>

    suspend fun getTopChartStream(subType: String) : Flow<PagingData<TopChartMedia>>

    suspend fun getPopularChart(type: String) : Resource<List<PopularChartMedia>>

    suspend fun getTopChart(subType: String) : Resource<List<String>>

    suspend fun getBatchMedias(batchQueryList: List<String>) : Resource<List<MediaMetadata>>

    suspend fun getMedia(movieId: String): Resource<MediaInfo>

    suspend fun getSuggestedMedias(mediaKeyword: String): Resource<List<SuggestedMedia>>

    suspend fun searchMedia(
        mediaKeyword: String,
        page: Int? = null,
        limit: Int? = null
    ): Resource<SearchResultsDto>
}