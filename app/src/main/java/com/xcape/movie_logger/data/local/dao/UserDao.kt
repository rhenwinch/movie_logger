package com.xcape.movie_logger.data.local.dao

import androidx.room.*
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.model.user.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    suspend fun getUser(): User

    @Query("SELECT * FROM user")
    fun getCurrentUser(): Flow<User>

    @Query("UPDATE user SET watchlist = :watchlist")
    suspend fun updateWatchlist(watchlist: List<WatchlistMedia>)

    @Query("UPDATE user SET reviews = :reviews")
    suspend fun updateReviews(reviews: List<WatchedMedia>)

    @Query("UPDATE user SET friends = :friends")
    suspend fun updateFriends(friends: List<User>)

    @Query("UPDATE user SET notifications = :notifications")
    suspend fun updateNotifications(notifications: List<Notification>)
    
    @Query("UPDATE user SET friendRequests = :friendRequest")
    suspend fun updateFriendRequests(friendRequest: List<FriendRequest>)

    @Query("UPDATE user SET preferences = :preferences")
    suspend fun updatePreferences(preferences: UserPreferences)

    @Query("UPDATE user SET unseenNotifications = :unseenNotifications")
    suspend fun updateUnseenNotifications(unseenNotifications: Int)

    @Query("UPDATE user SET unseenFriendRequests = :unseenFriendRequests")
    suspend fun updateUnseenFriendRequests(unseenFriendRequests: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)

    @Query("DELETE FROM user")
    suspend fun removeUser()
}