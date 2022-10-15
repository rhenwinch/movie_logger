package com.xcape.movie_logger.domain.model.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import java.util.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey val userId: String = "",
    @ColumnInfo(name = "fcmToken") val fcmToken: String = "",
    @ColumnInfo(name = "username") val username: String = "",
    @ColumnInfo(name = "bio") val bio: String = "",
    @ColumnInfo(name = "imageBanner") val imageBanner: String = "",
    @ColumnInfo(name = "imageProfile") val imageProfile: String = "",
    @ColumnInfo(name = "preferences") val preferences: UserPreferences = UserPreferences(),
    @ColumnInfo(name = "watchlist") val watchlist: List<WatchlistMedia> = listOf(),
    @ColumnInfo(name = "unseenNotifications") val unseenNotifications: Int = 0,
    @ColumnInfo(name = "unseenFriendRequests") val unseenFriendRequests: Int = 0,
    @ColumnInfo(name = "notifications") val notifications: List<Notification> = listOf(),
    @ColumnInfo(name = "friendRequests") val friendRequests: List<FriendRequest> = listOf(),
    @ColumnInfo(name = "reviews") val reviews: List<WatchedMedia> = listOf(),
    @ColumnInfo(name = "friends") val friends: List<User> = listOf()
)
