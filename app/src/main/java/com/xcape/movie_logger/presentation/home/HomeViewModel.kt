package com.xcape.movie_logger.presentation.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.repository.MovieRepository
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.utils.Constants.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    fun initializeCredentials() {
        viewModelScope.launch {
            when(val result = repository.getCredentials()) {
                is Resource.Success -> {
                    Log.d(TAG, "All requirements initialized!")
                }
                is Resource.Error -> {
                    Log.e(TAG, "Error thrown: ${result.message}")
                }
            }
        }
    }

}