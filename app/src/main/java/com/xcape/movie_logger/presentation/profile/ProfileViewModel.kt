package com.xcape.movie_logger.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.g
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository
) : ViewModel() {

    val state: StateFlow<ProfileUIState>
    val accept: (ProfileUIAction) -> Unit

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    init {
        val actionStateFlow = MutableSharedFlow<ProfileUIAction>()

        val isSigningOut = actionStateFlow
            .filterIsInstance<ProfileUIAction.SignOut>()
            .distinctUntilChanged()

        state = isSigningOut.map { signOut ->
            var state = ProfileUIState()

            if(signOut.isSigningOut) {
                signOutCurrentUser()
                state = state.copy(isSignedOut = true)
            }

            return@map state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUIState()
        )

        viewModelScope.launch {
            launch { getCurrentUser().collect { _userData.value = it } }
            launch { getLatestFriends().collect { _friends.value = it } }
        }

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private suspend fun signOutCurrentUser() {
        authRepository.signOut()
    }

    private fun getCurrentUser(): Flow<User> {
        return localUserRepository.getCurrentUser()
    }

    private fun getLatestFriends(): Flow<List<User>> {
        try {
            val friendsList = remoteUserRepository.getLatestFriends()

            viewModelScope.launch {
                friendsList.collect { localUserRepository.updateFriends(it) }
            }
        }
        catch (_: Exception) {}

        return localUserRepository.getLatestFriendsList()
    }
}