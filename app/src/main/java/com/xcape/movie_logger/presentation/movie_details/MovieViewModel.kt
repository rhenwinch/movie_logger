package com.xcape.movie_logger.presentation.movie_details

import androidx.lifecycle.*
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.repository.local.WatchedMediasRepository
import com.xcape.movie_logger.domain.repository.local.WatchlistRepository
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovieViewModel @Inject constructor(
    private val remoteRepository: MovieRemoteRepository,
    private val localWatchlistRepository: WatchlistRepository,
    private val localWatchedListRepository: WatchedMediasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var _mediaData: MediaInfo? = null

    private val isInWatchlist: Flow<MenuState?>
    val state: StateFlow<MediaUIState>
    val mediaData: Flow<MediaInfo?>
    val accept: (MediaUIAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<MediaUIAction>()
        val queryId = savedStateHandle.get<String>(MEDIA_ID)

        val isRetrying = actionStateFlow
            .filterIsInstance<MediaUIAction.Retry>()
            .onStart { emit(MediaUIAction.Retry(isClicked = null)) }

        val isRemovingFromWatchlist = actionStateFlow
            .filterIsInstance<MediaUIAction.RemoveFromWatchlist>()
            .onStart { emit(MediaUIAction.RemoveFromWatchlist(isClicked = false)) }

        val isAddingToWatchlist = actionStateFlow
            .filterIsInstance<MediaUIAction.AddToWatchlist>()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .distinctUntilChanged()
            .onStart { emit(MediaUIAction.AddToWatchlist(isClicked = false)) }

        mediaData = isRetrying
            .flatMapLatest { retry ->
                if(retry.isClicked == null || retry.isClicked == true) {
                    queryId?.let { id ->
                        getMedia(id)
                    }!!
                }
                else {
                    flowOf(_mediaData)
                }
            }

        isInWatchlist = combine(
            isAddingToWatchlist,
            isRemovingFromWatchlist,
            ::Pair
        ).flatMapLatest { (add, remove) ->
            val isInWatchedList = isMediaAlreadyInWatchedList(queryId)
            val isInWatchlist = isMediaAlreadyInWatchlist(queryId)

            // If it is not in any of our repositories, add it.
            if(!isInWatchlist && !isInWatchedList && add.isClicked) {
                saveInWatchlist()
                flowOf(MenuState.ON_WATCHLIST)
            }
            // If it is already in the watchlist, remove it.
            else if(isInWatchlist && remove.isClicked) {
                removeInWatchlist()
                flowOf(MenuState.NOT_ON_ANY)
            }
            else if(isInWatchlist) {
                flowOf(MenuState.ON_WATCHLIST)
            }
            else if(isInWatchedList) {
                flowOf(MenuState.ON_WATCHED_LIST)
            }
            else flowOf(null)
        }

        state = isInWatchlist
            .map { menuState ->
                MediaUIState(isAlreadyInDatabase = menuState)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = MediaUIState()
            )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getMedia(mediaId: String): Flow<MediaInfo?> {
        return flow {
            val mediaFromLocal = localWatchlistRepository.getWatchlistMediaByMediaId(mediaId = mediaId)
            if(mediaFromLocal == null) {
                when(val result = remoteRepository.getMedia(mediaId)) {
                    is Resource.Success -> {
                        _mediaData = result.data
                        emit(result.data)
                    }
                    is Resource.Error -> {
                        emit(null)
                    }
                }
            }
            else {
                _mediaData = mediaFromLocal.mediaInfo
                emit(mediaFromLocal.mediaInfo)
            }
        }
    }

    private suspend fun isMediaAlreadyInWatchlist(movieId: String?): Boolean {
        val watchlistMedia = movieId?.let { localWatchlistRepository.getWatchlistMediaByMediaId(it) }
        return watchlistMedia != null
    }

    private suspend fun isMediaAlreadyInWatchedList(movieId: String?): Boolean {
        val watchedMedia = movieId?.let { localWatchedListRepository.getWatchedMediaByMediaId(it) }
        return watchedMedia != null
    }

    private suspend fun saveInWatchlist() {
        _mediaData?.let { media ->
            val watchlistMedia = WatchlistMedia(
                id = media.id,
                addedOn = Date(),
                dateReleased = media.dateReleased,
                rating = media.rating,
                title = media.title,
                mediaInfo = media
            )

            localWatchlistRepository.insertWatchlistMedia(watchlistMedia)
        }
    }

    private suspend fun removeInWatchlist() {
        _mediaData?.let { media ->
            localWatchlistRepository.deleteWatchlistMediaById(media.id)
        }
    }
}