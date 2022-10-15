package com.xcape.movie_logger.domain.model.auth

data class Credentials(
    val accessKey: String,
    val sKey: String,
    val sessionToken: String
)