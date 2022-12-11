package com.xcape.movie_logger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xcape.movie_logger.domain.model.auth.Credentials
import com.xcape.movie_logger.domain.model.auth.FCMCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface FCMCredentialsDao {
    @Query("SELECT * FROM fcmToken")
    suspend fun getToken(): FCMCredentials

    @Query("SELECT * FROM fcmToken")
    fun getCurrentToken(): Flow<FCMCredentials>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: FCMCredentials)

    @Query("DELETE FROM fcmToken")
    suspend fun removeCredentials()
}