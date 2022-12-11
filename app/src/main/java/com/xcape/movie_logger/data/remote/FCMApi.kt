package com.xcape.movie_logger.data.remote

import com.xcape.movie_logger.data.dto.FCMRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMApi {
    @Headers("Cache-control: no-cache")
    @POST("/index.php?type=send")
    suspend fun sendToFCM(@Body request: FCMRequest)
}