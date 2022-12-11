package com.xcape.movie_logger.presentation.search

import android.util.Log
import androidx.lifecycle.*
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.SuggestedMedia
import com.xcape.movie_logger.domain.repository.remote.MediaRepository
import com.xcape.movie_logger.common.Constants
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val LAST_SEARCH_QUERY = "last_search_query"
const val LAST_PAGE_SCROLLED = "last_page_scrolled"
const val DEFAULT_QUERY = ""
const val DEFAULT_PAGE = 1
const val PAGE_LIMIT = 10

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val remoteMediaRepository: MediaRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val initialQuery: String = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
    private val lastPageScrolled: Int = savedStateHandle[LAST_PAGE_SCROLLED] ?: DEFAULT_PAGE

    private val _suggestedQueries: MutableLiveData<List<SuggestedMedia>> = MutableLiveData(emptyList())
    val suggestedQueries: LiveData<List<SuggestedMedia>> = _suggestedQueries

    private val _searchResults: MutableLiveData<List<MediaInfo>> = MutableLiveData()
    val searchResults: LiveData<List<MediaInfo>> = _searchResults

    private val _state = MutableStateFlow(SearchUIState())
    val state: StateFlow<SearchUIState> = _state

    init {
        // Get recommended medias fromUserId remote API
        searchMedia(queryString = initialQuery,
            page = lastPageScrolled)
    }

    fun onEvent(event: SearchUIAction) {
        when(event) {
            is SearchUIAction.Typing -> _state.update {
                suggestQueries(event.charactersTyped)

                it.copy(lastCharactersTyped = event.charactersTyped)
            }
            is SearchUIAction.FocusOnSearchBox -> _state.update {
                it.copy(isTyping = event.isFocused)
            }
            is SearchUIAction.ChangePage -> _state.update {
                searchMedia(_state.value.query, page = event.currentPage)

                it.copy(lastPageQueried = event.currentPage)
            }
            is SearchUIAction.Filter -> _state.update {
                it.copy(filters = event.type)
            }
            is SearchUIAction.Search -> _state.update {
                searchMedia(event.query, page = lastPageScrolled)

                it.copy(
                    query = event.query,
                    lastPageQueried = lastPageScrolled,
                    hasNotSearchedBefore = event.query == initialQuery,
                    totalResults = -1
                )
            }
        }
    }

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = state.value.query
        savedStateHandle[LAST_PAGE_SCROLLED] = state.value.lastPageQueried
        super.onCleared()
    }

    private fun searchMedia(
        queryString: String,
        page: Int
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when(val result = remoteMediaRepository.searchMedia(
                    queryString,
                    page = page,
                    limit = PAGE_LIMIT,
                    filters = _state.value.filters
            )) {
                is Resource.Success -> {
                    _searchResults.value = result.data!!.data!!

                    _state.update {
                        it.copy(maxPages = result.data.totalPages,
                        isLoading = false,
                        totalResults = result.data.totalResults)
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = null) }
                    Log.e(Constants.APP_TAG, result.message ?: "Unknown Error")
                }
            }
        }
    }

    private fun suggestQueries(queryString: String) {
        viewModelScope.launch {
            when(val result = remoteMediaRepository.getSuggestedMedias(queryString)) {
                is Resource.Success -> _suggestedQueries.value = result.data ?: emptyList()
                is Resource.Error -> _suggestedQueries.value = emptyList()
            }
        }
    }
}