package com.xcape.movie_logger.data.remote

import com.xcape.movie_logger.data.dto.MediaMetadata
import com.xcape.movie_logger.data.dto.SearchResultsDto
import com.xcape.movie_logger.domain.model.auth.Credentials
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.PopularChartMedia
import com.xcape.movie_logger.domain.model.media.SuggestedMedia
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface IMDBApi {
    @Headers("Cache-control: no-cache")
    @GET("index.php")
    suspend fun getPopularChart(
        @Query("a_key") accessKey: String,
        @Query("s_key") secretKey: String,
        @Query("s_token") sessionToken: String,
        @Query("type") type: String
    ) : List<PopularChartMedia>

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=boxoffice")
    suspend fun getBoxOffice(
        @Query("a_key") accessKey: String,
        @Query("s_key") secretKey: String,
        @Query("s_token") sessionToken: String,
    ) : List<MediaMetadata>

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=top")
    suspend fun getTopChart(
        @Query("a_key") accessKey: String,
        @Query("s_key") secretKey: String,
        @Query("s_token") sessionToken: String,
        @Query("sub_type") subType: String
    ) : List<String>

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=movie")
    suspend fun getMovie(
        @Query("a_key") accessKey: String,
        @Query("s_key") secretKey: String,
        @Query("s_token") sessionToken: String,
        @Query("id") movieId: String
    ): MediaInfo

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=batch")
    suspend fun getBatchMovies(
        @Query("a_key") accessKey: String,
        @Query("s_key") secretKey: String,
        @Query("s_token") sessionToken: String,
        @Query("batch") batchQuery: String
    ): List<MediaMetadata>

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=suggest")
    suspend fun getSuggestedMedias(
        @Query("id") mediaKeyword: String
    ): List<SuggestedMedia>

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=search")
    suspend fun searchMedia(
        @Query("a_key") accessKey: String,
        @Query("s_key") secretKey: String,
        @Query("s_token") sessionToken: String,
        @Query("id") mediaKeyword: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): SearchResultsDto

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=credentials")
    suspend fun getCredentials(): Credentials
}