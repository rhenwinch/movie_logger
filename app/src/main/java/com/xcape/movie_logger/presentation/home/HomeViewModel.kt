package com.xcape.movie_logger.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository
import com.xcape.movie_logger.domain.utils.Resource
import com.xcape.movie_logger.domain.utils.Constants.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRemoteRepository
) : ViewModel() {

    fun initializeCredentials() {
        viewModelScope.launch {
            var isInitialized = false
            var errorMessage: String? = null

            // 3 retries until repository obtains the Credentials from the backend
            for (i in 1..3) {
                when(val result = repository.getCredentials()) {
                    is Resource.Success -> {
                        isInitialized = true
                        break
                    }
                    is Resource.Error -> {
                        errorMessage = result.message
                        continue
                    }
                }
            }

            if(isInitialized)
                Log.d(TAG, "Initialized")
            else
                Log.e(TAG, "Not initialized <=> $errorMessage")
        }
    }

}