package com.xcape.movie_logger.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.common.Constants.APP_ID
import com.xcape.movie_logger.common.Constants.APP_TAG
import com.xcape.movie_logger.data.local.dao.FCMCredentialsDao
import com.xcape.movie_logger.data.utils.DispatchersProvider
import com.xcape.movie_logger.domain.model.auth.FCMCredentials
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.repository.remote.UsersRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.m
import java.util.*
import javax.inject.Inject

interface NotificationBuilder {
    fun createChannel(notificationManager: NotificationManager)
    fun createNotification(message: RemoteMessage)
    fun createPendingActivity(): PendingIntent
}

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService(), NotificationBuilder {
    @Inject
    lateinit var usersRepository: UsersRepository
    @Inject
    lateinit var authRepository: AuthRepository
    @Inject
    lateinit var dispatchersProvider: DispatchersProvider
    @Inject
    lateinit var fcmCredentialsDao: FCMCredentialsDao

    private val job = SupervisorJob()
    private lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(dispatchersProvider.io + job)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val user = authRepository.getAuthUser()

        if(user?.uid == message.data["toUserId"]) {
            super.onMessageReceived(message)

            Log.d(APP_TAG, "From: ${message.from}")
            message.notification?.let {
                Log.d(APP_TAG, "Message Notification Body: ${it.body}")
            }

            createNotification(message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(APP_TAG, "Refreshed token: $token")
        updateAppTokenOnServer(token)
    }

    override fun createNotification(message: RemoteMessage) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(notificationManager)

        // Load large image
        val largeIcon = if(message.notification?.icon != null) {
             Picasso.get()
                 .load(message.notification?.icon)
                 .placeholder(R.drawable.profile_placeholder)
                 .get()
        } else { ContextCompat.getDrawable(this, R.drawable.profile_placeholder)?.toBitmap() }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, APP_ID)
            .setAutoCancel(true)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setSound(defaultSoundUri)

        notificationManager.notify(Calendar.getInstance().timeInMillis.toInt(), notificationBuilder.build());
    }

    override fun createChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            APP_ID,
            APP_TAG,
            IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun createPendingActivity(): PendingIntent {
        TODO("Not yet implemented")
    }

    private fun updateAppTokenOnServer(token: String) {
        scope.launch {
            authRepository.getCurrentAuthUser().collect {
                // If user is not null
                if(it != null) {
                    usersRepository.updateUserField(userId = it.uid,
                        field = "fcmToken",
                        value = FieldValue.arrayUnion(token))
                }
                // Else save it to cache, then wait for user to login and let
                // FCMDao handle the process of saving it to our DB.
                else {
                    fcmCredentialsDao.removeCredentials()
                    fcmCredentialsDao.saveToken(FCMCredentials(token = token))
                }
            }
        }
    }
}