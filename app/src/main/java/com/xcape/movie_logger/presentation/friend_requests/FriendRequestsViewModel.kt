package com.xcape.movie_logger.presentation.friend_requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.repository.remote.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestsViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository
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
        return friendsRepository.getLatestFriendRequests()
    }
}