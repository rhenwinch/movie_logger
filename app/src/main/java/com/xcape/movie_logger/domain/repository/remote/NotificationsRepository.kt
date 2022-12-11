package com.xcape.movie_logger.domain.repository.remote

import com.xcape.movie_logger.domain.model.user.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {
    fun getLatestNotifications(): Flow<List<Notification>>
    fun getLatestUnseenNotifications(): Flow<Int>

    fun updateNotifications(to: String,  item: Notification)
    fun updateUnseenNotifications(notifications: Int)
    //suspend fun deleteNotification()
}