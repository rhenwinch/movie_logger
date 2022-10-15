package com.xcape.movie_logger.presentation.add_item

sealed class AddItemUIAction {
    data class Typing(val charactersTyped: String? = null): AddItemUIAction()
    data class Rate(val ratingGiven: Float = 0F): AddItemUIAction()
    data class Submit(val isSubmitting: Boolean = false): AddItemUIAction()
    data class ErrorConsume(val isConsumed: Boolean = false): AddItemUIAction()
}

data class AddItemUIState(
    val isTyping: Boolean = false,
    val isAdded: Boolean = false,
    val allErrorsWereConsumed: Boolean = false,
    val isRatingError: Boolean = false,
    val ratingError: Throwable? = null,
    val lastRatingGiven: Float = 0F,
    val lastCharactersTyped: String? = null,
)