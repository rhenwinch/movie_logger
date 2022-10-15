package com.xcape.movie_logger.presentation.friend_requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestsViewModel @Inject constructor(
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository
) : ViewModel() {
    private val _friendRequests = MutableLiveData<List<FriendRequest>>()
    val friendRequests: LiveData<List<FriendRequest>> = _friendRequests

    init {
        viewModelScope.launch {
            getLatestFriendRequests().collect {
                _friendRequests.value = it
            }
        }
    }

    private fun getLatestFriendRequests(): Flow<List<FriendRequest>> {
        try {
            val friendRequests = remoteUserRepository.getLatestFriendRequests()

            viewModelScope.launch {
                friendRequests.collect { localUserRepository.updateFriendRequests(it) }
            }
        }
        catch (_: Exception) {}

        return localUserRepository.getLatestFriendRequests()
    }
}