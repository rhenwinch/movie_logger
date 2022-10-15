package com.xcape.movie_logger.presentation.movie_details

enum class MenuState {
    ON_WATCHED_LIST,
    ON_WATCHLIST,
    NOT_ON_ANY
}

sealed class MediaUIAction {
    data class AddToWatchlist(val isClicked: Boolean): MediaUIAction()
    data class RemoveFromWatchlist(val isClicked: Boolean): MediaUIAction()
    data class Retry(val isClicked: Boolean?): MediaUIAction()
}

data class MediaUIState(
    var isRetrying: Boolean = true,
    val isAlreadyInDatabase: MenuState? = null
)
