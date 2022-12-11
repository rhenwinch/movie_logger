package com.xcape.movie_logger.data.repository.remote

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.xcape.movie_logger.data.dto.MediaMetadata
import com.xcape.movie_logger.data.dto.SearchResultsDto
import com.xcape.movie_logger.data.dto.toBoxOffice
import com.xcape.movie_logger.data.remote.MediaApi
import com.xcape.movie_logger.data.repository.paging_source.PopularChartPagingSource
import com.xcape.movie_logger.data.repository.paging_source.TopChartPagingSource
import com.xcape.movie_logger.domain.model.auth.Credentials
import com.xcape.movie_logger.domain.model.media.*
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.domain.repository.remote.MediaRepository
import com.xcape.movie_logger.common.Constants.PAGE_SIZE
import com.xcape.movie_logger.common.Functions.getCurrentTimestamp
import com.xcape.movie_logger.data.local.dao.MediaAPICredentialsDao
import com.xcape.movie_logger.presentation.search.SearchFilterType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val api: MediaApi,
    private val credentialsDao: MediaAPICredentialsDao
) : MediaRepository {
    override suspend fun getCredentials(): Credentials? {
        return try {
            var credentials = credentialsDao.getCredentials()
            if(credentials != null && credentials.expirationDate > getCurrentTimestamp()) {
                return credentials
            }

            credentials = api.getCredentials()
            credentialsDao.removeCredentials()
            credentialsDao.saveCredentials(credentials)
            credentials
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getBoxOffice(): Resource<List<BoxOfficeMedia>> {
        return try {
            val credentials = getCredentials()!!
            val listOfMedias = api.getBoxOffice(
                accessKey = credentials.accessKey,
                secretKey = credentials.sKey,
                sessionToken = credentials.sessionToken,
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
            val credentials = getCredentials()!!
            val listOfMedias = api.getPopularChart(
                accessKey = credentials.accessKey,
                secretKey = credentials.sKey,
                sessionToken = credentials.sessionToken,
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
            val credentials = getCredentials()!!
            val listOfMedias = api.getTopChart(
                accessKey = credentials.accessKey,
                secretKey = credentials.sKey,
                sessionToken = credentials.sessionToken,
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
            val credentials = getCredentials()!!
            val batchQuery = batchQueryList.joinToString("%26") { "ids%3D${it}" }
            val listOfMedias = api.getBatchMovies(
                accessKey = credentials.accessKey,
                secretKey = credentials.sKey,
                sessionToken = credentials.sessionToken,
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
            val credentials = getCredentials()!!
            val listOfMedias = api.getMovie(
                accessKey = credentials.accessKey,
                secretKey = credentials.sKey,
                sessionToken = credentials.sessionToken,
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
        limit: Int?,
        filters: SearchFilterType
    ): Resource<SearchResultsDto> {
        return try {
            val credentials = getCredentials()!!
            val filterType = when(filters) {
                SearchFilterType.Movies -> "movie|tvMovie"
                SearchFilterType.MoviesAndTvShows -> "movie|tvSeries|tvMovie"
                SearchFilterType.TvShows -> "tvSeries"
                SearchFilterType.UserProfiles -> ""
            }

            val searchResults = api.searchMedia(
                accessKey = credentials.accessKey,
                secretKey = credentials.sKey,
                sessionToken = credentials.sessionToken,
                mediaKeyword = mediaKeyword,
                page = if(page == 1) null else page,
                limit = limit,
                filters = filterType
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