package com.xcape.movie_logger.presentation.movie_details

import androidx.lifecycle.*
import com.xcape.movie_logger.domain.repository.MovieRepository
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var movieId: String

    private val _state: MutableStateFlow<MovieUIState> = MutableStateFlow(MovieUIState())
    val state: StateFlow<MovieUIState> = _state

    private val _movieTitle: MutableLiveData<String> = MutableLiveData("")
    val movieTitle: LiveData<String> = _movieTitle


    init {
        savedStateHandle.get<String>(MOVIE_ID)?.let {
            movieId = it
            getMovie(movieId)
        }
    }

    private fun getMovie(movieId: String) {
        viewModelScope.launch {
            when(val result = repository.getMovie(movieId)) {
                is Resource.Success -> {
                    _state.value = state.value.copy(
                        mediaData = result.data,
                        isLoading = false
                    )
                    _movieTitle.postValue(state.value.mediaData?.title)
                }
                is Resource.Error -> {
                    _state.value = state.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun retryMovieRequest() {
        getMovie(movieId)
    }
}