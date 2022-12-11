package com.xcape.movie_logger.domain.use_cases.form_validators

import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.model.auth.ValidatedResult
import com.xcape.movie_logger.common.Constants.USERS
import com.xcape.movie_logger.domain.use_cases.form_validators.base.FormValidator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvalidUsername(
    message: String?
) : Exception(message)

class UsernameValidator @Inject constructor(
    private val firestore: FirebaseFirestore
) : FormValidator<String> {
    override fun validate(item: String): ValidatedResult {
        if(item.isBlank()) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidUsername("Username can't be empty")
            )
        }
        else if(item.replace("[A-Za-z0-9._]".toRegex(), "").isNotBlank()) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidUsername("Symbols should only be _ and .")
            )
        }
        else if(item.matches("[.]{2,}".toRegex())) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidUsername("Username must not have consecutive dots")
            )
        }
        else if(item.startsWith(".") || item.endsWith(".")) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidUsername("Username must not start or end with a dot")
            )
        }
        else if(item.length < 4) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidUsername("Username should be greater than 3")
            )
        }
        else if(item.contains(" ")) {
            return ValidatedResult(
                isSuccessful = false,
                error = InvalidUsername("Username should not contain spaces")
            )
        }

        return ValidatedResult(isSuccessful = true)
    }

    suspend fun isUsernameNotTaken(item: String): ValidatedResult {
        return try {
            val foundDocuments = firestore.collection(USERS)
                .whereEqualTo("username", item)
                .get()
                .await()
                .documents

            if(foundDocuments.isNotEmpty()) {
                return ValidatedResult(
                    isSuccessful = false,
                    error = InvalidUsername(message = "Username is already taken")
                )
            }

            ValidatedResult(isSuccessful = true)
        }
        catch (e: Exception) {
            ValidatedResult(
                isSuccessful = false,
                error = InvalidUsername(message = e.localizedMessage)
            )
        }
    }
}