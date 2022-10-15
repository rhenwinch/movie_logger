package com.xcape.movie_logger.presentation.sign_up

enum class AuthFormType {
    EMAIL,
    USERNAME,
    PASSWORD,
    CONFIRM_PASSWORD
}

sealed class SignUpUIAction {
    data class EmailTyping(val isDoneTying: Boolean, val charactersTyped: String): SignUpUIAction()
    data class UsernameTyping(val isDoneTying: Boolean, val charactersTyped: String): SignUpUIAction()
    data class PasswordTyping(val isDoneTying: Boolean, val charactersTyped: String): SignUpUIAction()
    data class ConfirmPasswordTyping(val isDoneTying: Boolean, val charactersTyped: String): SignUpUIAction()
    data class Submit(val isClicked: Boolean): SignUpUIAction()
}

data class SignUpUIState(
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isAccountCreated: Boolean? = null
)