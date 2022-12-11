package com.xcape.movie_logger.domain.use_cases.form_validators

import com.xcape.movie_logger.domain.model.auth.ValidatedResult
import com.xcape.movie_logger.domain.use_cases.form_validators.base.FormValidator

class InvalidRating(
    val isGreaterThan5: Boolean = false,
    val isLessThan0: Boolean = false,
    message: String
) : Exception(message)

class RatingValidator : FormValidator<Float> {
    override fun validate(item: Float): ValidatedResult {
        if(item > 5.0) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidRating(
                    isGreaterThan5 = true,
                    message = "Rating must be not be greater than 5"
                )
            )
        }

        if(item <= 0) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidRating(
                    isLessThan0 = true,
                    message = "Rating must be not be greater than 5"
                )
            )
        }

        return ValidatedResult(isSuccessful = true)
    }
}