package com.xcape.movie_logger.domain.repository.local

import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.model.user.*
import kotlinx.coroutines.flow.Flow

interface LocalUserRepository {
    suspend fun saveUser(user: User)
    suspend fun removeUser()
    suspend fun getUser(): User
    fun getCurrentUser(): Flow<User>
    fun getCurrentUnseenNotifications(): Flow<Int>
    fun getCurrentUnseenFriendRequests(): Flow<Int>
    fun getLatestFriendsList(): Flow<List<User>>
    fun getLatestNotifications(): Flow<List<Notification>>
    fun getLatestFriendRequests(): Flow<List<FriendRequest>>
    suspend fun updateWatchlist(watchlist: List<WatchlistMedia>)
    suspend fun updateReviews(reviews: List<WatchedMedia>)
    suspend fun updateFriends(friends: List<User>)
    suspend fun updateNotifications(notifications: List<Notification>)
    suspend fun updateFriendRequests(friendRequest: List<FriendRequest>)
    suspend fun updateUnseenNotifications(notifications: Int)
    suspend fun updateUnseenFriendRequests(friendRequests: Int)
    suspend fun updatePreferences(preferences: UserPreferences)
    suspend fun updateRemoteUser(user: User? = null, field: String? = null, value: Any? = null)
}