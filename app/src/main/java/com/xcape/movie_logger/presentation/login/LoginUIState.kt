package com.xcape.movie_logger.presentation.login

import com.xcape.movie_logger.presentation.sign_up.AuthFormType

sealed class LoginUIAction {
    data class EmailTyping(val isDoneTyping: Boolean, val charactersTyped: String): LoginUIAction()
    data class PasswordTyping(val isDoneTyping: Boolean, val charactersTyped: String): LoginUIAction()
    data class Submit(val isClicked: Boolean): LoginUIAction()
}

data class LoginUIState(
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null,
    val isUserLoggedIn: Boolean = false,
)