package com.xcape.movie_logger.presentation.profile

sealed class ProfileUIAction {
    data class SignOut(val isSigningOut: Boolean = false): ProfileUIAction()
}

data class ProfileUIState(
    val isSignedOut: Boolean = true
)