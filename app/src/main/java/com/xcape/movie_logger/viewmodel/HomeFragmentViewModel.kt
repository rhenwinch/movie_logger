package com.xcape.movie_logger.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.xcape.movie_logger.database.Movie
import com.xcape.movie_logger.database.MovieDatabase
import com.xcape.movie_logger.repository.MovieRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class HomeFragmentViewModel(private val repo: MovieRepository): ViewModel() {
    // Obtain all movies
    var allMovies: LiveData<List<Movie>> = repo.allMovies.asLiveData()

    fun addMovie(movie: Movie) = viewModelScope.launch {
        repo.addMovie(movie)
    }
}

class HomeFragmentViewModelFactory(private val repo: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeFragmentViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel class")
    }

}