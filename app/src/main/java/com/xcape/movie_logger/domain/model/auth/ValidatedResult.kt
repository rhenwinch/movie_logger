package com.xcape.movie_logger.domain.model.auth

data class ValidatedResult(
    val isSuccessful: Boolean,
    val error: Throwable? = null
)