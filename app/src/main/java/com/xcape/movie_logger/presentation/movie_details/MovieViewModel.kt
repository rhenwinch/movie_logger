package com.xcape.movie_logger.presentation.movie_details

import androidx.lifecycle.*
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.repository.remote.MediaRepository
import com.xcape.movie_logger.domain.repository.remote.WatchedMediasRepository
import com.xcape.movie_logger.domain.repository.remote.WatchlistRepository
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class MediaState {
    IN_WATCHED_LIST,
    IN_WATCHLIST,
    NOT_IN_ANY
}

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val remoteRepository: MediaRepository,
    private val watchlistRepository: WatchlistRepository,
    private val watchedListRepository: WatchedMediasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val queryId = savedStateHandle.get<String>(MEDIA_ID)

    private val _mediaState = MutableLiveData(MediaState.NOT_IN_ANY)
    val mediaState: LiveData<MediaState> = _mediaState

    private val _mediaData = MutableLiveData<MediaInfo?>()
    val mediaData: LiveData<MediaInfo?> = _mediaData

    init {
        viewModelScope.launch {
            _mediaData.value = queryId?.let { getMedia(it) }
            _mediaState.value = if(isMediaAlreadyInWatchedList(queryId)) {
                MediaState.IN_WATCHED_LIST
            }
            else if(isMediaAlreadyInWatchlist(queryId)) {
                MediaState.IN_WATCHLIST
            }
            else {
                MediaState.NOT_IN_ANY
            }
        }

    }

    private suspend fun getMedia(mediaId: String): MediaInfo? {
        val mediaFromLocal = watchlistRepository.getWatchlistMediaByMediaId(mediaId = mediaId)
        return if(mediaFromLocal == null) {
            when(val result = remoteRepository.getMedia(mediaId)) {
                is Resource.Success -> { result.data }
                is Resource.Error -> { null }
            }
        }
        else { mediaFromLocal.mediaInfo }
    }

    private suspend fun isMediaAlreadyInWatchlist(movieId: String?): Boolean {
        val watchlistMedia = movieId?.let { watchlistRepository.getWatchlistMediaByMediaId(it) }
        return watchlistMedia != null
    }

    private suspend fun isMediaAlreadyInWatchedList(movieId: String?): Boolean {
        val watchedMedia = movieId?.let { watchedListRepository.getWatchedMediaByMediaId(it) }
        return watchedMedia != null
    }

    fun saveInWatchlist() {
        viewModelScope.launch {
            if(_mediaState.value == MediaState.NOT_IN_ANY) {
                _mediaData.value?.let {
                    val watchlistMedia = WatchlistMedia(
                        id = it.id,
                        addedOn = Date(),
                        dateReleased = it.dateReleased,
                        rating = it.rating,
                        title = it.title,
                        mediaInfo = it
                    )

                    watchlistRepository.insertWatchlistMedia(watchlistMedia)
                    _mediaState.value = MediaState.IN_WATCHLIST
                }
            }
        }
    }

    fun removeInWatchlist() {
        viewModelScope.launch {
            if(_mediaState.value == MediaState.IN_WATCHLIST) {
                _mediaData.value?.let { media ->
                    watchlistRepository.deleteWatchlistMediaById(media.id)
                    _mediaState.value = MediaState.NOT_IN_ANY
                }
            }
        }
    }

    fun retryFetch() {
        viewModelScope.launch {
            if (queryId != null) {
                getMedia(queryId)
            }
        }
    }
}