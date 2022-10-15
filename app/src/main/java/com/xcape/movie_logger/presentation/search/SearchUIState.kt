package com.xcape.movie_logger.presentation.search


sealed class SearchUIAction {
    data class Scroll(val currentQuery: String): SearchUIAction()
    data class Search(val query: String): SearchUIAction()
    data class Typing(val charactersTyped: String): SearchUIAction()
    data class FocusOnSearchBox(val isFocused: Boolean): SearchUIAction()
}

data class SearchUIState(
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val lastCharactersTyped: String = DEFAULT_QUERY,
    val isTyping: Boolean = false,
    val hasNotSearchedBefore: Boolean = true,
    val hasNotScrolledForCurrentSearch: Boolean = false
)

