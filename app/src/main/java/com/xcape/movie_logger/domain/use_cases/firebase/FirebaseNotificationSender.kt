package com.xcape.movie_logger.domain.use_cases.firebase

import com.xcape.movie_logger.common.Functions.getCurrentTimestamp
import com.xcape.movie_logger.data.dto.FCMCustomData
import com.xcape.movie_logger.data.dto.FCMNotificationPayload
import com.xcape.movie_logger.data.dto.FCMRequest
import com.xcape.movie_logger.data.dto.FCMRequestPayload
import com.xcape.movie_logger.data.remote.FCMApi
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.remote.NotificationsRepository
import java.util.*
import javax.inject.Inject

interface NotificationSender {
    suspend fun send(
        token: String,
        to: User,
        notification: Notification,
        payload: FCMNotificationPayload)
}

class FirebaseNotificationSender @Inject constructor(
    private val api: FCMApi,
    private val notificationsRepository: NotificationsRepository
) : NotificationSender {
    override suspend fun send(
        token: String,
        to: User,
        notification: Notification,
        payload: FCMNotificationPayload
    ) {
        try {
            val notificationsPayload = FCMRequest(FCMRequestPayload(
                to = token,
                notification = payload,
                data = FCMCustomData(to.userId)
            ))

            api.sendToFCM(request = notificationsPayload)
            notificationsRepository.updateNotifications(to.userId, notification)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
}