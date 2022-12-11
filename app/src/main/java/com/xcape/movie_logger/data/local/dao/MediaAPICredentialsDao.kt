package com.xcape.movie_logger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xcape.movie_logger.domain.model.auth.Credentials
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaAPICredentialsDao {
    @Query("SELECT * FROM credentials")
    suspend fun getCredentials(): Credentials?

    @Query("SELECT * FROM credentials")
    fun getCurrentCredentials(): Flow<Credentials?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCredentials(credentials: Credentials)

    @Query("DELETE FROM credentials")
    suspend fun removeCredentials()
}