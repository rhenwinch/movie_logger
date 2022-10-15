package com.xcape.movie_logger.domain.repository.remote

import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface RemoteUserRepository {
    suspend fun saveUser(userId: String, user: User, field: String? = null, value: Any? = null)

    suspend fun getUser(userId: String = ""): User?

    fun getLatestFriends(): Flow<List<User>>

    fun getLatestNotifications(): Flow<List<Notification>>

    fun getLatestFriendRequests(): Flow<List<FriendRequest>>

    fun getLatestUnseenNotifications(): Flow<Int>

    fun getLatestUnseenFriendRequests(): Flow<Int>
}