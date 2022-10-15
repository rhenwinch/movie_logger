package com.xcape.movie_logger.presentation.main_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository
) : ViewModel() {
    val isAuthenticated: Boolean = authRepository.isUserAlreadyAuthenticated()

    private val _unseenNotifications = MutableLiveData<Int>()
    val unseenNotifications: LiveData<Int> = _unseenNotifications

    private val _unseenFriendRequests = MutableLiveData<Int>()
    val unseenFriendRequests: LiveData<Int> = _unseenFriendRequests

    init {
        viewModelScope.launch {
            launch {
                if(isAuthenticated) {
                    getLatestNotifications().collect {
                        _unseenNotifications.value = it
                    }
                }
            }

            launch {
                if(isAuthenticated) {
                    getLatestFriendRequests().collect {
                        _unseenFriendRequests.value = it
                    }
                }
            }
        }
    }

    /*
    * Supposed to obtain latest
    * notifications/friend requests from
    * remote user repo; and save it to local
    */
    private fun getLatestNotifications(): Flow<Int> {
        try {
            val unseenNotifications = remoteUserRepository.getLatestUnseenNotifications()

            viewModelScope.launch {
                unseenNotifications.collect { localUserRepository.updateUnseenNotifications(it) }

                launch {

                }
            }
        }
        catch (_: Exception) {}

        return localUserRepository.getCurrentUnseenNotifications()
    }

    private fun getLatestFriendRequests(): Flow<Int> {
        try {
            val unseenFriendRequests = remoteUserRepository.getLatestUnseenFriendRequests()

            viewModelScope.launch {
                unseenFriendRequests.collect { localUserRepository.updateUnseenFriendRequests(it) }
            }
        }
        catch (_: Exception) {}

        return localUserRepository.getCurrentUnseenFriendRequests()
    }
}