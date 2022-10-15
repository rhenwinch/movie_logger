package com.xcape.movie_logger.data.repository.remote

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.xcape.movie_logger.data.dto.MediaMetadata
import com.xcape.movie_logger.data.dto.SearchResultsDto
import com.xcape.movie_logger.data.dto.toBoxOffice
import com.xcape.movie_logger.data.remote.IMDBApi
import com.xcape.movie_logger.data.repository.paging_source.PopularChartPagingSource
import com.xcape.movie_logger.data.repository.paging_source.SearchResultsPagingSource
import com.xcape.movie_logger.data.repository.paging_source.TopChartPagingSource
import com.xcape.movie_logger.domain.model.auth.Credentials
import com.xcape.movie_logger.domain.model.media.*
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository
import com.xcape.movie_logger.domain.utils.Constants.PAGE_SIZE
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRemoteRepositoryImpl @Inject constructor(
    private val api: IMDBApi
) : MovieRemoteRepository {
    private lateinit var apiCredentials: Credentials

    override suspend fun getCredentials(): Resource<Credentials> {
        return try {
            if(this::apiCredentials.isInitialized)
                Resource.Success(apiCredentials)
            else {
                val credentials = api.getCredentials()
                apiCredentials = credentials
                Resource.Success(apiCredentials)
            }
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage!!)
        }
    }

    override suspend fun getBoxOffice(): Resource<List<BoxOfficeMedia>> {
        return try {
            if(!this::apiCredentials.isInitialized) {
                delay(1500L)
            }
            val listOfMedias = api.getBoxOffice(
                accessKey = apiCredentials.accessKey,
                secretKey = apiCredentials.sKey,
                sessionToken = apiCredentials.sessionToken,
            ).map { it.toBoxOffice() }
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            getCredentials()
            Resource.Error("No credentials provided")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun getPopularChartStream(type: String): Flow<PagingData<PopularChartMedia>> {
        val popularChartData = getPopularChart(type)
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularChartPagingSource(popularChartData) }
        ).flow
    }

    override fun getSearchResultsStream(
        mediaKeyword: String,
        recommendedMediasOnly: Boolean
    ): Flow<PagingData<MediaInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchResultsPagingSource(
                this,
                mediaKeyword,
                recommendedMediasOnly
            ) }
        ).flow
    }

    override suspend fun getTopChartStream(subType: String): Flow<PagingData<TopChartMedia>> {
        val topChartData = getTopChart(subType)
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TopChartPagingSource(topChartData, this) }
        ).flow
    }

    override suspend fun getPopularChart(type: String): Resource<List<PopularChartMedia>> {
        return try {
            if(!this::apiCredentials.isInitialized) {
                delay(1500L)
            }
            val listOfMedias = api.getPopularChart(
                accessKey = apiCredentials.accessKey,
                secretKey = apiCredentials.sKey,
                sessionToken = apiCredentials.sessionToken,
                type
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun getTopChart(subType: String): Resource<List<String>> {
        return try {
            if(!this::apiCredentials.isInitialized) {
                delay(1500L)
            }

            val listOfMedias = api.getTopChart(
                accessKey = apiCredentials.accessKey,
                secretKey = apiCredentials.sKey,
                sessionToken = apiCredentials.sessionToken,
                subType
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun getBatchMedias(batchQueryList: List<String>): Resource<List<MediaMetadata>> {
        return try {
            if(!this::apiCredentials.isInitialized) {
                delay(1500L)
            }

            val batchQuery = batchQueryList.joinToString("%26") { "ids%3D${it}" }
            val listOfMedias = api.getBatchMovies(
                accessKey = apiCredentials.accessKey,
                secretKey = apiCredentials.sKey,
                sessionToken = apiCredentials.sessionToken,
                batchQuery
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun getMedia(movieId: String): Resource<MediaInfo> {
        return try {
            if(!this::apiCredentials.isInitialized) {
                delay(1500L)
            }

            val listOfMedias = api.getMovie(
                accessKey = apiCredentials.accessKey,
                secretKey = apiCredentials.sKey,
                sessionToken = apiCredentials.sessionToken,
                movieId
            )
            return Resource.Success(listOfMedias)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun getSuggestedMedias(mediaKeyword: String): Resource<List<SuggestedMedia>> {
        return try {
            val movie = api.getSuggestedMedias(mediaKeyword)
            Resource.Success(movie)
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun searchMedia(
        mediaKeyword: String,
        page: Int?,
        limit: Int?
    ): Resource<SearchResultsDto> {
        return try {
            if(!this::apiCredentials.isInitialized) {
                delay(1500L)
            }

            val searchResults = api.searchMedia(
                accessKey = apiCredentials.accessKey,
                secretKey = apiCredentials.sKey,
                sessionToken = apiCredentials.sessionToken,
                mediaKeyword = mediaKeyword,
                page = if(page == 1) null else page,
                limit = limit
            )
            
            return Resource.Success(searchResults)
        } catch(e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            Resource.Error("No credentials provided")
        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown Error")
        }
    }
}