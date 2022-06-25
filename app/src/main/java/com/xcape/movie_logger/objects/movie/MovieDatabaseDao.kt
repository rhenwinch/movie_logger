package com.xcape.movie_logger.objects.movie

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMovie(movie: Movie)

    @Update
    fun updateMovie(movie: Movie)

    //@Delete
    //fun removeMovie(id: Long)

    @Query("SELECT * from movie_database WHERE movieKey = :id")
    fun getMovie(id: Long): Movie?

    @Query("DELETE FROM movie_database")
    fun clearMovies()

    @Query("SELECT * FROM movie_database ORDER BY movieKey DESC LIMIT 1")
    fun getLatestMovie(): Movie?

    @Query("SELECT * FROM movie_database ORDER BY movieKey DESC")
    fun getAllMovies(): Flow<List<Movie>>
}