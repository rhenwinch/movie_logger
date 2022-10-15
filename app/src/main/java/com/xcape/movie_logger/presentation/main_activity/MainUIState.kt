package com.xcape.movie_logger.presentation.main_activity

sealed class MainUIAction {
    data class SignIn(
        val isSigningIn: Boolean,
        val email: String? = null,
        val password: String? = null
    ): MainUIAction()
}

data class MainUIState(
    val isSignedIn: Boolean = false
)