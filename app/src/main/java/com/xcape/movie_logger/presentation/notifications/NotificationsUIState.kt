package com.xcape.movie_logger.presentation.notifications

sealed class NotificationsUIAction {
    data class Scroll(val position: Int): NotificationsUIAction()
}

data class NotificationsUIState(
    val lastScrolledPosition: Int = 0
)

