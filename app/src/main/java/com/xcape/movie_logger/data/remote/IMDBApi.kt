package com.xcape.movie_logger.data.remote

import com.xcape.movie_logger.domain.model.Media
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.domain.model.PopularChart
import com.xcape.movie_logger.domain.model.ZuluCredentials
import com.xcape.movie_logger.domain.model.SuggestedMovie
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
    ) : List<PopularChart>

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
    ): Media

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
    suspend fun getSuggestedMovies(
        @Query("id") movieKeyword: String
    ): List<SuggestedMovie>

    @Headers("Cache-control: no-cache")
    @GET("index.php?type=credentials")
    suspend fun getCredentials(): ZuluCredentials
}