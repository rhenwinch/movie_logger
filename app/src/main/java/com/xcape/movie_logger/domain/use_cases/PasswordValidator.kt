package com.xcape.movie_logger.domain.use_cases

import com.xcape.movie_logger.domain.model.auth.ValidatedResult

class InvalidPassword(
    message: String
) : Exception(message)

class PasswordValidator : FormValidator<String> {
    override fun validate(item: String): ValidatedResult {
        if(item.isBlank()) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidPassword("Password can't be empty")
            )
        }
        else if(item.length < 6) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidPassword("Password should be at least 6 characters")
            )
        }

        return ValidatedResult(isSuccessful = true)
    }

    fun validateConfirmPassword(
        item: String,
        confirmedItem: String
    ): ValidatedResult {
        if(item != confirmedItem) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidPassword("Password don't match")
            )
        }

        return ValidatedResult(isSuccessful = true)
    }
}