package com.xcape.movie_logger.presentation.main_activity

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import com.xcape.movie_logger.domain.repository.remote.FriendsRepository
import com.xcape.movie_logger.domain.repository.remote.NotificationsRepository
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.repository.remote.MediaRepository
import com.xcape.movie_logger.common.Constants
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteMediasRepository: MediaRepository,
    private val notificationsRepository: NotificationsRepository,
    private val friendsRepository: FriendsRepository,
) : ViewModel() {
    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    private val _unseenNotifications = MutableLiveData<Int>()
    val unseenNotifications: LiveData<Int> = _unseenNotifications

    private val _unseenFriendRequests = MutableLiveData<Int>()
    val unseenFriendRequests: LiveData<Int> = _unseenFriendRequests

    init {
        getCurrentAuthUser()
        initializeCredentials()
        viewModelScope.launch {
            isAuthenticated.asFlow().collect {
                // If user is authenticated
                if(it) {
                    getLatestNotifications().combine(getLatestFriendRequests()) { notifications, friendRequests ->
                        _unseenNotifications.value = notifications
                        _unseenFriendRequests.value = friendRequests
                    }.collect()
                }
            }
        }
    }

    private fun getCurrentAuthUser() {
        viewModelScope.launch {
            authRepository.getCurrentAuthUser().collect {
                _isAuthenticated.value = it != null
            }
        }
    }

    /*
    * Supposed to obtain latest
    * notifications/friend requests fromUserId
    * remote user repo; and save it to local
    */
    private fun getLatestNotifications(): Flow<Int> {
       return notificationsRepository.getLatestUnseenNotifications()
    }

    private fun getLatestFriendRequests(): Flow<Int> {
        return friendsRepository.getLatestUnseenFriendRequests()
    }

    private fun initializeCredentials() {
        viewModelScope.launch {
            var isInitialized = false

            // 3 retries until remoteMediasRepository obtains the Credentials from the backend
            for (i in 1..3) {
                val result = remoteMediasRepository.getCredentials()
                if(result != null) {
                    isInitialized = true
                    break
                }
            }

            if(isInitialized)
                Log.d(Constants.APP_TAG, "Initialized")
        }
    }
}