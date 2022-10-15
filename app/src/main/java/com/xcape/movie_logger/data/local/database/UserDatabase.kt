package com.xcape.movie_logger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xcape.movie_logger.data.local.dao.UserDao
import com.xcape.movie_logger.data.local.type_converter.UserConverters
import com.xcape.movie_logger.domain.model.user.User

@Database(
    version = 1,
    entities = [User::class]
)
@TypeConverters(UserConverters::class)
abstract class UserDatabase: RoomDatabase() {
    abstract val userDao: UserDao

    companion object {
        const val USER_DATABASE = "user_database"
    }
}