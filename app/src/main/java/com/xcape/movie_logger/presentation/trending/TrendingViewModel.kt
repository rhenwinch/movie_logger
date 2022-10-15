package com.xcape.movie_logger.presentation.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.domain.utils.Functions.getCurrentTimestamp
import com.xcape.movie_logger.domain.utils.Functions.needsUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val POPULAR_MOVIE = "moviemeter"
const val POPULAR_TV = "tvmeter"
const val TOP_MOVIE = "movie"
const val TOP_TV = "tv"

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val repository: MovieRemoteRepository
) : ViewModel() {
    // UI States
    private val _trendingUIState = MutableStateFlow(TrendingUIState())
    val trendingUIState = _trendingUIState.asStateFlow()

    private var lastTimestampRefresh = getCurrentTimestamp() * 8

    // Jobs
    private val popularDataFetchJob: MutableMap<String, Job?> = mutableMapOf()
    private val topDataFetchJob: MutableMap<String, Job?> = mutableMapOf()
    private var boxOfficeDataFetchJob: Job? = null
    val accept: (TrendingUIAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<TrendingUIAction>()

        viewModelScope.launch {
            // Subscriber of hasFinishFetching state
            launch {
                trendingUIState.collect { state ->
                    if(state.popularChartData.size == 2
                        && state.topChartData.size == 2
                        && state.boxOfficeData != null
                    ) {
                        _trendingUIState.update {
                            it.copy(hasFinishedFetching = true)
                        }
                    }
                }
            }

            // Subscribe to latest events from our ui action callback
            launch {
                actionStateFlow.collectLatest { event ->
                    when(event) {
                        is TrendingUIAction.Refresh -> _trendingUIState.update {
                            it.copy(isRefreshing = event.triggerRefresh)
                        }
                    }
                }
            }
        }

        getPopularChart(type = POPULAR_MOVIE)
        getPopularChart(type = POPULAR_TV)
        getTopChart(subType = TOP_MOVIE)
        getTopChart(subType = TOP_TV)
        getBoxOffice()

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getPopularChart(type: String) {
        popularDataFetchJob[type] = viewModelScope.launch {
            try {
                val result = repository.getPopularChartStream(type).cachedIn(viewModelScope)
                _trendingUIState.update {
                    it.popularChartData[type] = result
                    it.copy(popularChartData = it.popularChartData)
                }
            }
            catch (e: Throwable) {
                e.printStackTrace()
                _trendingUIState.update {
                    it.copy(hasErrors = true)
                }
            }
        }
    }

    private fun getTopChart(subType: String) {
        topDataFetchJob[subType] = viewModelScope.launch {
            try {
                val result = repository.getTopChartStream(subType)
                _trendingUIState.update {
                    it.topChartData[subType] = result
                    it.copy(topChartData = it.topChartData)
                }
            }
            catch (e: Throwable) {
                e.printStackTrace()
                _trendingUIState.update {
                    it.copy(hasErrors = true)
                }
            }
        }
    }

    private fun getBoxOffice() {
        boxOfficeDataFetchJob = viewModelScope.launch {
            try {
                when(val result = repository.getBoxOffice()) {
                    is Resource.Success -> {
                        _trendingUIState.update {
                            it.copy(boxOfficeData = flowOf(result.data!!))
                        }
                    }
                    is Resource.Error -> {
                        _trendingUIState.update {
                            it.copy(hasErrors = true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _trendingUIState.update {
                    it.copy(hasErrors = true)
                }
            }
        }
    }

    fun cancelAllDataFetchJobs() {
        if(_trendingUIState.value.hasErrors) {
            popularDataFetchJob.forEach { (_, job) ->
                job?.cancel()
            }
            topDataFetchJob.forEach { (_, job) ->
                job?.cancel()
            }
            boxOfficeDataFetchJob?.cancel()
        }
    }

    fun refreshData(needsRetry: Boolean = false): Boolean {
        val updateInterval = 3600 * 8
        val currentTime = getCurrentTimestamp()
        return if(
            needsRetry
            || needsUpdate(currentTime, lastTimestampRefresh, updateInterval)
        ) {
            _trendingUIState.update {
                it.copy(
                    popularChartData = mutableMapOf(),
                    topChartData = mutableMapOf(),
                    boxOfficeData = null
                )
            }
            getPopularChart(type = "moviemeter")
            getPopularChart(type = "tvmeter")
            getTopChart(subType = "movie")
            getTopChart(subType = "tv")
            getBoxOffice()
            lastTimestampRefresh = currentTime
            true
        }
        else {
            false
        }
    }
}