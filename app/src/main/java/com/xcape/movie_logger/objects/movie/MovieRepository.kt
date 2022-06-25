package com.xcape.movie_logger.objects.movie

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class MovieRepository(private val daoSource: MovieDatabaseDao) {
    val allMovies: Flow<List<Movie>> = daoSource.getAllMovies()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addMovie(movie: Movie) {
        daoSource.addMovie(movie)
    }
}