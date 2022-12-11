package com.xcape.movie_logger.domain.repository.remote

import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    fun getLatestFriends(): Flow<List<User>>
    fun getLatestFriendRequests(): Flow<List<FriendRequest>>
    fun getLatestUnseenFriendRequests(): Flow<Int>

    fun updateFriends(friends: List<String>)
    fun updateFriendRequests(item: FriendRequest)
    fun updateUnseenFriendRequests(friendRequests: Int)
}