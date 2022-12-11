package com.xcape.movie_logger.presentation.home

import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.user.Post

sealed class HomeUIAction() {
    data class Like(val ownerId: String, val postId: String, val isDisliking: Boolean = false) :
        HomeUIAction()

    data class AddToWatchlist(val post: Post, val isRemoving: Boolean = false) :
        HomeUIAction()

    data class Rate(val mediaInfo: MediaInfo, val isUnrating: Boolean = false) : HomeUIAction()
}

class HomeUIState {
}