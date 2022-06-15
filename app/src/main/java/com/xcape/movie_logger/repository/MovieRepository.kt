package com.xcape.movie_logger.repository

import androidx.annotation.WorkerThread
import com.xcape.movie_logger.database.Movie
import com.xcape.movie_logger.database.MovieDatabaseDao
import kotlinx.coroutines.flow.Flow

class MovieRepository(private val daoSource: MovieDatabaseDao) {
    val allMovies: Flow<List<Movie>> = daoSource.getAllMovies()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addMovie(movie: Movie) {
        daoSource.addMovie(movie)
    }
}