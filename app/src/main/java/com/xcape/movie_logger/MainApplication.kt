package com.xcape.movie_logger

import android.app.Application
import com.xcape.movie_logger.database.MovieDatabase
import com.xcape.movie_logger.repository.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MovieApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { MovieDatabase.getInstance(this, applicationScope) }
    val repository by lazy { MovieRepository(database.movieDatabaseDao) }
}