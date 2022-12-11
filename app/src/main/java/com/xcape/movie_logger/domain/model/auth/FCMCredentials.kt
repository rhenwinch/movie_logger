package com.xcape.movie_logger.domain.model.auth

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.xcape.movie_logger.common.Functions
import com.xcape.movie_logger.domain.model.media.WatchedMedia

@Entity(tableName = "fcmToken")
data class FCMCredentials(@PrimaryKey val token: String)