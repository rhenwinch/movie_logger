package com.xcape.movie_logger.presentation.trailer_dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TrailerViewModel : ViewModel() {
    private val _currentItem: MutableLiveData<Int> = MutableLiveData(0)
    val currentItem: LiveData<Int> = _currentItem

    private val _playbackPosition: MutableLiveData<Long> = MutableLiveData(0)
    val playbackPosition: LiveData<Long> = _playbackPosition

    private val _playWhenReady: MutableLiveData<Boolean> = MutableLiveData(true)
    val playWhenReady: LiveData<Boolean> = _playWhenReady

    fun updatePlaybackPosition(currentPosition: Long) {
        _playbackPosition.postValue(currentPosition)
    }

    fun updatePlaybackItem(currentItem: Int) {
        _currentItem.postValue(currentItem)
    }

    fun updatePlayWhenReady(shouldPlay: Boolean) {
        _playWhenReady.postValue(shouldPlay)
    }
}