package com.xcape.movie_logger.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MovieDatabaseDao {
    @Insert
    fun addMovie(movie: Movie)

    @Update
    fun updateMovie(movie: Movie)

    @Query("SELECT * from movie_database WHERE movieId = :id")
    fun getMovie(id: Long)

    @Query("DELETE FROM movie_database")
    fun clearMovies()

    @Query("DELETE FROM movie_database WHERE movieId = :id")
    fun removeMovie(id: Long)

    @Query("SELECT * FROM movie_database ORDER BY movieId DESC LIMIT 1")
    fun getLatestMovie(): Movie?

    @Query("SELECT * FROM movie_database ORDER BY movieId DESC")
    fun getAllMovies(): LiveData<List<Movie>>
}