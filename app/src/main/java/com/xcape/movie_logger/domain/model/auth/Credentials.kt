package com.xcape.movie_logger.domain.model.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Credentials(
    @PrimaryKey val accessKey: String,
    val sKey: String,
    val sessionToken: String,
    val expirationDate: Long
)