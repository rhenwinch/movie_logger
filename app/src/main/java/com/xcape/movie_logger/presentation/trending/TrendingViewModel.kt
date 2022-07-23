package com.xcape.movie_logger.presentation.trending

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.domain.model.PopularChart
import com.xcape.movie_logger.domain.repository.MovieRepository
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.utils.Constants.PAGE_SIZE
import com.xcape.movie_logger.utils.Functions.getCurrentTimestamp
import com.xcape.movie_logger.utils.Functions.needsUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_PROGRESS = 100

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {
    private var dataCount: Int = 0
    
    private val _listOfPopularStates: MutableMap<String, MutableStateFlow<TrendingUIState<PopularChart>>> = mutableMapOf(
        "moviemeter" to MutableStateFlow(TrendingUIState("moviemeter")),
        "tvmeter" to MutableStateFlow(TrendingUIState("tvmeter"))
    )
    private val _listOfTopStates: MutableMap<String, MutableStateFlow<TrendingUIState<MediaMetadata>>> = mutableMapOf(
        "movie" to MutableStateFlow(TrendingUIState("movie")),
        "tv" to MutableStateFlow(TrendingUIState("tv"))
    )
    private val _boxOfficeState: MutableStateFlow<TrendingUIState<MediaMetadata>> = MutableStateFlow(TrendingUIState("boxoffice"))
    private val _viewPagerPosition: MutableLiveData<Int> = MutableLiveData(5)
    private val _lastTimestampRefresh: MutableLiveData<Long> = MutableLiveData(getCurrentTimestamp() * 8)

    // MAX_PROGRESS of data count must be ready to be loaded on the UI; else, throw error
    private var dataProgressValue: Double = 0.0
    private val _dataProgress: MutableLiveData<Double> = MutableLiveData(dataProgressValue)

    val listOfPopularStates: Map<String, StateFlow<TrendingUIState<PopularChart>>> = mapOf(
        "moviemeter" to _listOfPopularStates["moviemeter"]!!,
        "tvmeter" to _listOfPopularStates["tvmeter"]!!
    )
    val listOfTopStates: Map<String, StateFlow<TrendingUIState<MediaMetadata>>> = mapOf(
        "movie" to _listOfTopStates["movie"]!!,
        "tv" to _listOfTopStates["tv"]!!
    )
    val boxOfficeState: StateFlow<TrendingUIState<MediaMetadata>> = _boxOfficeState
    val viewPagerPosition: LiveData<Int> = _viewPagerPosition
    val dataProgress: LiveData<Double> = _dataProgress

    init {
        getPopularChart(type = "moviemeter")
        getPopularChart(type = "tvmeter")
        getTopChart(subType = "movie")
        getTopChart(subType = "tv")
        getBoxOffice()
        dataCount = 5
    }

    private fun getPopularChart(
        type: String,
        retry: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if(retry) _listOfPopularStates[type]!!.value = TrendingUIState(type)
                when(val result = repository.getPopularChartSource(type)) {
                    is Resource.Success -> {
                        val flow = Pager(
                            config = PagingConfig(pageSize = PAGE_SIZE.toInt()),
                            pagingSourceFactory = { result.data!! }
                        ).flow.cachedIn(viewModelScope)

                        _listOfPopularStates[type]!!.value = listOfPopularStates[type]!!.value.copy(
                            flowPagingData = flow,
                            isLoading = false
                        )
                        dataProgressValue += MAX_PROGRESS / dataCount
                        _dataProgress.postValue(dataProgressValue)
                    }
                    is Resource.Error -> {
                        _listOfPopularStates[type]!!.value = listOfPopularStates[type]!!.value.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                }
            }
            catch (e: Throwable) {
                e.printStackTrace()
                _listOfPopularStates[type]!!.value = listOfPopularStates[type]!!.value.copy(
                    errorMessage = "Error thrown: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }
    
    private fun getTopChart(
        subType: String,
        retry: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if(retry) _listOfTopStates[subType]!!.value = TrendingUIState(subType)
                when(val result = repository.getTopChartSource(subType)) {
                    is Resource.Success -> {
                        val flow = Pager(
                            config = PagingConfig(pageSize = PAGE_SIZE.toInt()),
                            pagingSourceFactory = { result.data!! }
                        ).flow.cachedIn(viewModelScope)

                        _listOfTopStates[subType]!!.value = listOfTopStates[subType]!!.value.copy(
                            flowPagingData = flow,
                            isLoading = false
                        )
                        dataProgressValue += MAX_PROGRESS / dataCount
                        _dataProgress.postValue(dataProgressValue)
                    }
                    is Resource.Error -> {
                        _listOfTopStates[subType]!!.value = listOfTopStates[subType]!!.value.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                }
            }
            catch (e: Throwable) {
                e.printStackTrace()
                _listOfTopStates[subType]!!.value = listOfTopStates[subType]!!.value.copy(
                    errorMessage = "Error thrown: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    private fun getBoxOffice(retry: Boolean = false) {
        viewModelScope.launch {
            try {
                if(retry) _boxOfficeState.value = TrendingUIState("boxoffice")
                when(val result = repository.getBoxOffice()) {
                    is Resource.Success -> {
                        val flow: Flow<List<MediaMetadata>> = flowOf(result.data!!)
                        _boxOfficeState.value = boxOfficeState.value.copy(
                            flowListData = flow,
                            isLoading = false
                        )
                        dataProgressValue += MAX_PROGRESS / dataCount
                        _dataProgress.postValue(dataProgressValue)
                    }
                    is Resource.Error -> {
                        _boxOfficeState.value = boxOfficeState.value.copy(
                            errorMessage = "Error thrown: ${result.message}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _boxOfficeState.value = boxOfficeState.value.copy(
                    errorMessage = "Error thrown: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun setViewPagerLastPosition(position: Int) {
        _viewPagerPosition.postValue(position)
    }

    fun refreshData(
        needsRetry: Boolean = false
    ): Boolean {
        val updateInterval = 3600 * 8
        val currentTime = getCurrentTimestamp()
        return if(needsRetry || needsUpdate(currentTime, _lastTimestampRefresh.value!!, updateInterval)) {
            dataProgressValue = 0.0
            _dataProgress.value = 0.0
            getPopularChart(
                type = "moviemeter",
                retry = true
            )
            getPopularChart(
                type = "tvmeter",
                retry = true
            )
            getTopChart(
                subType = "movie",
                retry = true
            )
            getTopChart(
                subType = "tv",
                retry = true
            )
            getBoxOffice(retry = true)
            _lastTimestampRefresh.postValue(currentTime)
            true
        }
        else {
            false
        }
    }
}