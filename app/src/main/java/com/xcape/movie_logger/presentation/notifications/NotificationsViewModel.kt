package com.xcape.movie_logger.presentation.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.domain.repository.remote.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    init {
        resetUnseenNotifications()

        viewModelScope.launch {
            getLatestNotifications().collect {
                _notifications.value = it
            }
        }
    }

    private fun getLatestNotifications(): Flow<List<Notification>> {
        return notificationsRepository.getLatestNotifications()
    }

    private fun resetUnseenNotifications() {
        notificationsRepository.updateUnseenNotifications(0)
    }
}