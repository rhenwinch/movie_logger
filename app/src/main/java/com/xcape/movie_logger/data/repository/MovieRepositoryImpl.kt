package com.xcape.movie_logger.data.repository

import android.os.Handler
import android.os.Looper
import com.xcape.movie_logger.domain.model.Media
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.domain.model.PopularChart
import com.xcape.movie_logger.domain.model.ZuluCredentials
import com.xcape.movie_logger.data.remote.IMDBApi
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.domain.model.SuggestedMovie
import com.xcape.movie_logger.domain.repository.MovieRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: IMDBApi
) : MovieRepository {
    private lateinit var zuluCredentials: ZuluCredentials

    override suspend fun getCredentials(): Resource<ZuluCredentials> {
        return try {
            if(this::zuluCredentials.isInitialized)
                Resource.Success(zuluCredentials)
            else {
                val credentials = api.getCredentials()
                zuluCredentials = credentials
                Resource.Success(zuluCredentials)
            }
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage!!)
        }
    }

    override suspend fun getBoxOffice(): Resource<List<MediaMetadata>> {
        return try {
            if(!this::zuluCredentials.isInitialized) {
                delay(1500L)
            }
            val listOfMedias = api.getBoxOffice(
                accessKey = zuluCredentials.accessKey,
                secretKey = zuluCredentials.sKey,
                sessionToken = zuluCredentials.sessionToken,
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            getCredentials()
            Resource.Error("No credentials provided: ${e.localizedMessage}")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error("Error thrown: ${e.localizedMessage}")
        }
    }

    override suspend fun getPopularChartSource(type: String): Resource<PopularChartPagingSource> {
        return Resource.Success(PopularChartPagingSource(getPopularChart(type)))
    }

    override suspend fun getTopChartSource(subType: String): Resource<TopChartPagingSource> {
        return Resource.Success(TopChartPagingSource(getTopChart(subType), this))
    }

    override suspend fun getPopularChart(type: String): Resource<List<PopularChart>> {
        return try {
            if(!this::zuluCredentials.isInitialized) {
                delay(1500L)
            }
            val listOfMedias = api.getPopularChart(
                accessKey = zuluCredentials.accessKey,
                secretKey = zuluCredentials.sKey,
                sessionToken = zuluCredentials.sessionToken,
                type
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided: ${e.localizedMessage}")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error("Error thrown: ${e.localizedMessage}")
        }
    }

    override suspend fun getTopChart(subType: String): Resource<List<String>> {
        return try {
            if(!this::zuluCredentials.isInitialized) {
                delay(1500L)
            }

            val listOfMedias = api.getTopChart(
                accessKey = zuluCredentials.accessKey,
                secretKey = zuluCredentials.sKey,
                sessionToken = zuluCredentials.sessionToken,
                subType
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided: ${e.localizedMessage}")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error("Error thrown: ${e.localizedMessage}")
        }
    }

    override suspend fun getBatchMovies(batchQueryList: List<String>): Resource<List<MediaMetadata>> {
        return try {
            if(!this::zuluCredentials.isInitialized) {
                delay(1500L)
            }

            val batchQuery = batchQueryList.joinToString("%26") { "ids%3D${it}" }
            val listOfMedias = api.getBatchMovies(
                accessKey = zuluCredentials.accessKey,
                secretKey = zuluCredentials.sKey,
                sessionToken = zuluCredentials.sessionToken,
                batchQuery
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided: ${e.localizedMessage}")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error("Error thrown: ${e.localizedMessage}")
        }
    }

    override suspend fun getMovie(movieId: String): Resource<Media> {
        return try {
            if(!this::zuluCredentials.isInitialized) {
                delay(1500L)
            }

            val listOfMedias = api.getMovie(
                accessKey = zuluCredentials.accessKey,
                secretKey = zuluCredentials.sKey,
                sessionToken = zuluCredentials.sessionToken,
                movieId
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided: ${e.localizedMessage}")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error("Error thrown: ${e.localizedMessage}")
        }
    }

    override suspend fun getSuggestedMovies(movieKeyword: String): Resource<List<SuggestedMovie>> {
        return try {
            val movie = api.getSuggestedMovies(movieKeyword)
            Resource.Success(movie)
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error("Error thrown: ${e.localizedMessage}")
        }
    }
}