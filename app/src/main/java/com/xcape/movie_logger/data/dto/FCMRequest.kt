package com.xcape.movie_logger.data.dto

data class FCMRequest(
    val payload: FCMRequestPayload
)

data class FCMRequestPayload(
    val to: String,
    val notification: FCMNotificationPayload,
    val data: FCMCustomData
)


data class FCMNotificationPayload(
    val title: String,
    val body: String,
    val icon: String? = null
)

data class FCMCustomData(val toUserId: String)