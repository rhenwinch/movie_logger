package com.xcape.movie_logger.utils

object Constants {
    const val MAIN_ENDPOINT: String = "https://xietiti.000webhostapp.com/"
    const val TAG = "MovieLogger"
    const val COLLECTION_PARENT = "movies"
    const val DOCUMENT_PARENT = "cached_movie_list"
    const val PAGE_SIZE = 10L // Page size per load
    const val TIMEZONE = "Asia/Manila"
    const val UPDATE_TIMER = 3600 * 24 // Update per day
}