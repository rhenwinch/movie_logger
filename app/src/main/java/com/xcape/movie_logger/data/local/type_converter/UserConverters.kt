package com.xcape.movie_logger.data.local.type_converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.model.user.*
import com.xcape.movie_logger.domain.utils.Functions
import java.util.*

@ProvidedTypeConverter
class UserConverters {
    // Settings
    @TypeConverter
    fun stringifySettings(userPreferences: UserPreferences?): String? {
        if(userPreferences == null)
            return null

        return Functions.parseToJson(userPreferences)
    }

    @TypeConverter
    fun parseStringSettings(userSettingsStr: String?): UserPreferences? {
        if(userSettingsStr == null)
            return null

        return Functions.parseFromJson(userSettingsStr)
    }
    // End of settings


    // Watchlist
    @TypeConverter
    fun stringifyWatchlist(watchlist: List<WatchlistMedia>?): String? {
        if(watchlist == null)
            return null

        return Functions.parseToJson(watchlist)
    }

    @TypeConverter
    fun parseStringWatchlist(watchlistStr: String?): List<WatchlistMedia>? {
        if(watchlistStr == null)
            return null

        return Functions.parseFromJson(watchlistStr)
    }
    // End of watchlist

    // Reviews
    @TypeConverter
    fun stringifyReviews(reviews: List<WatchedMedia>?): String? {
        if(reviews == null)
            return null

        return Functions.parseToJson(reviews)
    }

    @TypeConverter
    fun parseStringReviews(reviewsStr: String?): List<WatchedMedia>? {
        if(reviewsStr == null)
            return null

        return Functions.parseFromJson(reviewsStr)
    }
    // End of reviews


    // Friends
    @TypeConverter
    fun stringifyFriends(friends: List<User>?): String? {
        if(friends == null)
            return null

        return Functions.parseToJson(friends)
    }

    @TypeConverter
    fun parseStringFriends(friendsStr: String?): List<User>? {
        if(friendsStr == null)
            return null

        return Functions.parseFromJson(friendsStr)
    }
    // End of friends

    // Date
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
    // End of date



    // Notification
    @TypeConverter
    fun stringifyNotifications(notifications: List<Notification>?): String? {
        if(notifications == null)
            return null

        return Functions.parseToJson(notifications)
    }

    @TypeConverter
    fun parseStringNotifications(notificationStr: String?): List<Notification>? {
        if(notificationStr == null)
            return null

        return Functions.parseFromJson(notificationStr)
    }
    // End of Notification


    // Friend Requests
    @TypeConverter
    fun stringifyFriendRequests(friendRequests: List<FriendRequest>?): String? {
        if(friendRequests == null)
            return null

        return Functions.parseToJson(friendRequests)
    }

    @TypeConverter
    fun parseStringFriendRequests(friendRequestStr: String?): List<FriendRequest>? {
        if(friendRequestStr == null)
            return null

        return Functions.parseFromJson(friendRequestStr)
    }
    // End of FriendRequests
}