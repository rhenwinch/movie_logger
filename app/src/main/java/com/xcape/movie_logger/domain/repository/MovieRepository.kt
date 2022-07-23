package com.xcape.movie_logger.domain.repository

import com.xcape.movie_logger.domain.model.Media
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.domain.model.PopularChart
import com.xcape.movie_logger.domain.model.ZuluCredentials
import com.xcape.movie_logger.data.repository.PopularChartPagingSource
import com.xcape.movie_logger.data.repository.TopChartPagingSource
import com.xcape.movie_logger.domain.model.SuggestedMovie
import com.xcape.movie_logger.domain.utils.Resource

interface MovieRepository {
    suspend fun getCredentials() : Resource<ZuluCredentials>

    suspend fun getBoxOffice() : Resource<List<MediaMetadata>>

    suspend fun getPopularChartSource(type: String) : Resource<PopularChartPagingSource>

    suspend fun getTopChartSource(subType: String) : Resource<TopChartPagingSource>

    suspend fun getPopularChart(type: String) : Resource<List<PopularChart>>

    suspend fun getTopChart(subType: String) : Resource<List<String>>

    suspend fun getBatchMovies(batchQueryList: List<String>) : Resource<List<MediaMetadata>>

    suspend fun getMovie(movieId: String): Resource<Media>

    suspend fun getSuggestedMovies(movieKeyword: String): Resource<List<SuggestedMovie>>
}