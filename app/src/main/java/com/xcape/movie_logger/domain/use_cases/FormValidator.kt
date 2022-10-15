package com.xcape.movie_logger.domain.use_cases

import com.xcape.movie_logger.domain.model.auth.ValidatedResult

interface FormValidator<Form> {
    fun validate(item: Form): ValidatedResult
}