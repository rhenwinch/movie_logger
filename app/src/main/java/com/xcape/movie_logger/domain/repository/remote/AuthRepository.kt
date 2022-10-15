package com.xcape.movie_logger.domain.repository.remote

import com.xcape.movie_logger.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isUserAlreadyAuthenticated(): Boolean
    suspend fun signUp(email: String, username: String, password: String): Resource<Boolean>
    suspend fun signIn(email: String, password: String): Resource<Boolean>
    suspend fun signOut(): Resource<Boolean>
}