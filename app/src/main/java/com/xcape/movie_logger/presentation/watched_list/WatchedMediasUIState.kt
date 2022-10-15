package com.xcape.movie_logger.presentation.watched_list

import android.view.View

sealed class WatchedMediasUIAction {
    data class Swipe(val currentItemView: View? = null): WatchedMediasUIAction()
}

data class WatchedMediasUIState(
    val currentSwipedItem: View? = null
)