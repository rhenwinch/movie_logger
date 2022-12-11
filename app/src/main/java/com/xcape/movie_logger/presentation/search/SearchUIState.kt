package com.xcape.movie_logger.presentation.search

sealed interface SearchFilterType {
    object Movies : SearchFilterType
    object TvShows : SearchFilterType
    object MoviesAndTvShows : SearchFilterType
    object UserProfiles : SearchFilterType
}


sealed class SearchUIAction {
    data class ChangePage(val currentPage: Int) : SearchUIAction()
    data class Search(val query: String) : SearchUIAction()
    data class Typing(val charactersTyped: String) : SearchUIAction()
    data class Filter(val type: SearchFilterType) : SearchUIAction()
    data class FocusOnSearchBox(val isFocused: Boolean) : SearchUIAction()
}

data class SearchUIState(
    val query: String = DEFAULT_QUERY,
    val lastCharactersTyped: String = DEFAULT_QUERY,
    val maxPages: Int = 1,
    val totalResults: Int = 0,
    val lastPageQueried: Int = 1,
    val filters: SearchFilterType = SearchFilterType.MoviesAndTvShows,
    val isLoading: Boolean? = true,
    val isTyping: Boolean = false,
    val hasNotSearchedBefore: Boolean = true,
    val hasNotScrolledForCurrentSearch: Boolean = false
)

