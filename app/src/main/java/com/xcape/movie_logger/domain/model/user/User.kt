package com.xcape.movie_logger.domain.model.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import java.util.*

data class User(
    val userId: String = "",
    val fcmToken: List<String> = listOf(),
    val username: String = "",
    val bio: String = "",
    val imageBanner: String = "",
    val imageProfile: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val unseenNotifications: Int = 0,
    val unseenFriendRequests: Int = 0,
    val friends: List<String> = listOf()
)
