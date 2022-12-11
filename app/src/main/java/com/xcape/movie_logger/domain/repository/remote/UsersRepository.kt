package com.xcape.movie_logger.domain.repository.remote

import com.xcape.movie_logger.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    fun saveUser(userId: String = "", user: User)
    fun updateUserField(userId: String? = null, field: String, value: Any? = null)
    fun updateUserCollection(userId: String? = null, collection: String, documentId: String? = null,  value: Any)
    suspend fun getUser(userId: String? = null): User?
    fun getLiveUser(userId: String? = null): Flow<User>
}