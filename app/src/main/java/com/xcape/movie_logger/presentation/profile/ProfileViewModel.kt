package com.xcape.movie_logger.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val friendsRepository: FriendsRepository,
    private val watchlistRepository: WatchlistRepository,
    private val watchedMediasRepository: WatchedMediasRepository
) : ViewModel() {
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _friends = MutableLiveData<List<User>>(emptyList())
    val friends: LiveData<List<User>> = _friends

    private val _reviews = MutableStateFlow<List<WatchedMedia>>(emptyList())
    val reviews: StateFlow<List<WatchedMedia>> = _reviews
    private val _watchlist = MutableStateFlow<List<WatchlistMedia>>(emptyList())
    val watchlist: StateFlow<List<WatchlistMedia>> = _watchlist

    init {
        viewModelScope.launch {
            launch { getCurrentUser().collect { _userData.value = it } }
            launch { getLatestFriends().collect { _friends.value = it } }
            launch { getLatestReviews().collect { _reviews.value = it } }
            launch { getLatestWatchlist().collect { _watchlist.value = it } }
        }
    }

    fun onEvent(event: ProfileUIAction) {
        when(event) {
            is ProfileUIAction.SignOut -> signOutCurrentUser()
        }
    }

    private fun signOutCurrentUser() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    private fun getCurrentUser(): Flow<User> {
        return usersRepository.getLiveUser()
    }

    private fun getLatestFriends(): Flow<List<User>> {
        return friendsRepository.getLatestFriends()
    }

    private fun getLatestReviews(): Flow<List<WatchedMedia>> {
        return watchedMediasRepository.getLatestWatchedMedias()
    }

    private fun getLatestWatchlist(): Flow<List<WatchlistMedia>> {
        return watchlistRepository.getLatestWatchlistMedias()
    }
}