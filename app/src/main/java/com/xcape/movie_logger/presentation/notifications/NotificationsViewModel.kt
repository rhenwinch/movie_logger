package com.xcape.movie_logger.presentation.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository
) : ViewModel() {

    val state: StateFlow<NotificationsUIState>
    val accept: (NotificationsUIAction) -> Unit

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    init {
        val actionStateFlow = MutableSharedFlow<NotificationsUIAction>()

        val isScrolling = actionStateFlow.filterIsInstance<NotificationsUIAction.Scroll>()
            .distinctUntilChanged()
            .onStart { emit(NotificationsUIAction.Scroll(0)) }

        state = isScrolling.map {
            NotificationsUIState(
                lastScrolledPosition = it.position
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationsUIState()
        )

        viewModelScope.launch {
            getLatestNotifications().collect {
                _notifications.value = it
            }
        }

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getLatestNotifications(): Flow<List<Notification>> {
        try {
            val notifications = remoteUserRepository.getLatestNotifications()

            viewModelScope.launch {
                notifications.collect {
                    localUserRepository.updateUnseenNotifications(0) // Reset notification badge count
                    localUserRepository.updateNotifications(it)
                }
            }
        }
        catch (_: Exception) {}

        return localUserRepository.getLatestNotifications()
    }
}