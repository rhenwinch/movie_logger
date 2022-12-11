package com.xcape.movie_logger.presentation.profile

sealed interface ProfileUIAction {
    object SignOut: ProfileUIAction
}

data class ProfileUIState(
    val isSignedOut: Boolean = true
)