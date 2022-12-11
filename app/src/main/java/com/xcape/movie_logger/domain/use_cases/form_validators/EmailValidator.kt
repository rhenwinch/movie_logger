package com.xcape.movie_logger.domain.use_cases.form_validators

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.xcape.movie_logger.domain.model.auth.ValidatedResult
import com.xcape.movie_logger.domain.use_cases.form_validators.base.FormValidator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvalidEmail(
    message: String
) : Exception(message)

class EmailValidator @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : FormValidator<String> {
    override fun validate(item: String): ValidatedResult {
        if(item.isBlank()) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidEmail("Email can't be empty")
            )
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(item).matches()) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidEmail("Email can't be validated")
            )
        }

        return ValidatedResult(isSuccessful = true)
    }

    suspend fun isEmailNotTaken(email: String): ValidatedResult {
        return try {
            val task = firebaseAuth.fetchSignInMethodsForEmail(email).await()
            val errorMessage: String?
                = if(task.signInMethods?.isEmpty() == true) {
                    null
                } else {
                    "Email is already taken."
                }

            ValidatedResult(
                isSuccessful = errorMessage == null,
                error = errorMessage?.let { InvalidEmail(it) }
            )
        }
        catch (e: Exception) {
            ValidatedResult(
                isSuccessful = false,
                error = InvalidEmail("Something went wrong")
            )
        }
    }
}