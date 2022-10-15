package com.xcape.movie_logger.presentation.search

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.SuggestedMedia
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val LAST_SEARCH_QUERY = "last_search_query"
const val LAST_QUERY_SCROLLED = "last_query_scrolled"
const val DEFAULT_QUERY = ""

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRemoteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: StateFlow<SearchUIState>
    val searchResultsData: Flow<PagingData<MediaInfo>>
    private val _suggestedQueries: MutableLiveData<List<SuggestedMedia>> = MutableLiveData(emptyList())
    val suggestedQueries: LiveData<List<SuggestedMedia>> = _suggestedQueries
    val accept: (SearchUIAction) -> Unit

    init {
        val initialQuery: String = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
        val lastQueryScrolled: String = savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<SearchUIAction>()

        val searches = actionStateFlow
            .filterIsInstance<SearchUIAction.Search>()
            .onStart { emit(SearchUIAction.Search(query = initialQuery)) }

        val charactersTyped = actionStateFlow
            .filterIsInstance<SearchUIAction.Typing>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(SearchUIAction.Typing(charactersTyped = initialQuery)) }

        val searchBoxFocusOnSearchBox = actionStateFlow
            .filterIsInstance<SearchUIAction.FocusOnSearchBox>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(SearchUIAction.FocusOnSearchBox(isFocused = false)) }

        val queriesScrolled = actionStateFlow
            .filterIsInstance<SearchUIAction.Scroll>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(SearchUIAction.Scroll(currentQuery = lastQueryScrolled)) }

        state = combine(
            combine(searches, queriesScrolled, ::Pair),
            combine(charactersTyped, searchBoxFocusOnSearchBox, ::Pair),
            ::Pair
        ).map { (firstSet, secondSet) ->
            SearchUIState(
                query = firstSet.first.query,
                lastQueryScrolled = firstSet.second.currentQuery,
                lastCharactersTyped = secondSet.first.charactersTyped,
                isTyping = secondSet.second.isFocused,
                hasNotSearchedBefore = firstSet.first.query == initialQuery,
                hasNotScrolledForCurrentSearch = firstSet.first.query != firstSet.second.currentQuery
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = SearchUIState()
        )

        viewModelScope.launch {
            charactersTyped
                .collectLatest {
                    _suggestedQueries.postValue(suggestQueries(it.charactersTyped))
                }
        }

        searchResultsData = searches
            .flatMapLatest {
                searchMedia(
                    queryString = it.query,
                    recommendedMediasOnly = state.value.hasNotSearchedBefore
                )
            }
            .cachedIn(viewModelScope)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = state.value.query
        savedStateHandle[LAST_QUERY_SCROLLED] = state.value.lastQueryScrolled
        super.onCleared()
    }

    private fun searchMedia(
        queryString: String,
        recommendedMediasOnly: Boolean
    ): Flow<PagingData<MediaInfo>> {
        return repository.getSearchResultsStream(queryString, recommendedMediasOnly)
    }

    private suspend fun suggestQueries(queryString: String): List<SuggestedMedia> {
        return when(val result = repository.getSuggestedMedias(queryString)) {
            is Resource.Success -> result.data ?: emptyList()
            is Resource.Error -> emptyList()
        }
    }
}