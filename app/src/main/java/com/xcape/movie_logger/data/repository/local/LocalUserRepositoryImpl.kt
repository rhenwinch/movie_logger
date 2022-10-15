package com.xcape.movie_logger.data.repository.local

import com.xcape.movie_logger.data.local.dao.UserDao
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.model.user.*
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.local.WatchedMediasRepository
import com.xcape.movie_logger.domain.repository.local.WatchlistRepository
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class LocalUserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    private val watchlistRepository: WatchlistRepository,
    private val watchedMediasRepository: WatchedMediasRepository,
    private val remoteUserRepository: RemoteUserRepository
) : LocalUserRepository {
    override suspend fun saveUser(user: User) {
        removeUser() // Safe call to delete past saved users
        updateRemoteUser(user)
        dao.saveUser(user)
    }

    override suspend fun updateNotifications(notifications: List<Notification>) {
        dao.updateNotifications(notifications)
        updateRemoteUser(field = "notifications", value = notifications)
    }

    override suspend fun updateFriendRequests(friendRequest: List<FriendRequest>) {
        dao.updateFriendRequests(friendRequest)
        updateRemoteUser(field = "friendRequests", value = friendRequest)
    }

    override suspend fun updateUnseenNotifications(notifications: Int) {
        dao.updateUnseenNotifications(notifications)
        updateRemoteUser(field = "unseenNotifications", value = notifications)
    }

    override suspend fun updateUnseenFriendRequests(friendRequests: Int) {
        dao.updateUnseenFriendRequests(friendRequests)
        updateRemoteUser(field = "unseenFriendRequests", value = friendRequests)
    }

    override suspend fun updateWatchlist(watchlist: List<WatchlistMedia>) {
        dao.updateWatchlist(watchlist)
        watchlistRepository.updateUserWatchlist()
        updateRemoteUser(field = "watchlist", value = watchlist)
    }

    override suspend fun updateReviews(reviews: List<WatchedMedia>) {
        dao.updateReviews(reviews)
        updateRemoteUser(field = "reviews", value = reviews)
    }

    override suspend fun updateFriends(friends: List<User>) {
        dao.updateFriends(friends)
        updateRemoteUser(field = "friends", value = friends)
    }

    override suspend fun updatePreferences(preferences: UserPreferences) {
        dao.updatePreferences(preferences)
        updateRemoteUser(field = "preferences", value = preferences)
    }

    override suspend fun updateRemoteUser(
        user: User?,
        field: String?,
        value: Any?
    ) {
        try {
            val localUser = user ?: dao.getUser()
            remoteUserRepository.saveUser(localUser.userId, localUser, field, value)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun removeUser()
        = dao.removeUser()

    override fun getCurrentUser(): Flow<User>
        = dao.getCurrentUser()

    override fun getCurrentUnseenNotifications(): Flow<Int> {
        return callbackFlow {
            dao.getCurrentUser().collect {
                trySend(it.unseenNotifications)
            }

            awaitClose { this.cancel() }
        }
    }

    override fun getCurrentUnseenFriendRequests(): Flow<Int> {
        return callbackFlow {
            dao.getCurrentUser().collect {
                trySend(it.unseenFriendRequests)
            }

            awaitClose { this.cancel() }
        }
    }

    override fun getLatestFriendsList(): Flow<List<User>> {
        return callbackFlow {
            dao.getCurrentUser().collect {
                trySend(it.friends)
            }

            awaitClose { this.cancel() }
        }
    }

    override fun getLatestFriendRequests(): Flow<List<FriendRequest>> {
        return callbackFlow { 
            dao.getCurrentUser().collect {
                trySend(it.friendRequests)
            }
            
            awaitClose { this.cancel() }
        }
    }

    override fun getLatestNotifications(): Flow<List<Notification>> {
        return callbackFlow {
            dao.getCurrentUser().collect {
                trySend(it.notifications)
            }

            awaitClose { this.cancel() }
        }
    }

    override suspend fun getUser(): User = dao.getUser()
}