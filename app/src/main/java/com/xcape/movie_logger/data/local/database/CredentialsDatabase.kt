package com.xcape.movie_logger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xcape.movie_logger.data.local.dao.FCMCredentialsDao
import com.xcape.movie_logger.data.local.dao.MediaAPICredentialsDao
import com.xcape.movie_logger.domain.model.auth.Credentials
import com.xcape.movie_logger.domain.model.auth.FCMCredentials

@Database(
    entities = [Credentials::class, FCMCredentials::class],
    version = 1
)
abstract class CredentialsDatabase : RoomDatabase() {
    abstract val fcmCredentialsDao: FCMCredentialsDao
    abstract val mediaApiCredentialsDao: MediaAPICredentialsDao

    companion object {
        const val CREDENTIALS_DATABASE = "credentials_database"
    }
}