package com.xcape.movie_logger.presentation.watched_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.repository.remote.WatchedMediasRepository
import com.xcape.movie_logger.presentation.common.MediaSorter
import com.xcape.movie_logger.presentation.watchlist.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchedMediasViewModel @Inject constructor(
    private val watchlistRepository: WatchedMediasRepository
) : ViewModel(), MediaSorter<WatchedMedia> {
    val state: StateFlow<WatchedMediasUIState>
    val watchedMediaData: Flow<List<WatchedMedia>>
    val accept: (WatchedMediasUIAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<WatchedMediasUIAction>()

        val onSwipe = actionStateFlow
            .filterIsInstance<WatchedMediasUIAction.Swipe>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(WatchedMediasUIAction.Swipe()) }

        state = onSwipe
            .map {
                WatchedMediasUIState(
                    currentSwipedItem = it.currentItemView
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = WatchedMediasUIState()
            )

        viewModelScope.launch {

        }

        watchedMediaData = getSortedAddedMediasFromWatchedList(SortType.DESCENDING.ordinal)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getSortedPopularMediasFromWatchedList(sortType: Int): Flow<List<WatchedMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> getGreatestWatchedMedias { it.rating }
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> getLeastWatchedMedias { it.rating }
            else -> flowOf(emptyList())
        }
    }

    private fun getSortedReleasedMediasFromWatchedList(sortType: Int): Flow<List<WatchedMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> getGreatestWatchedMedias { it.dateReleased }
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> getLeastWatchedMedias { it.dateReleased }
            else -> flowOf(emptyList())
        }
    }

    private fun getSortedAddedMediasFromWatchedList(sortType: Int): Flow<List<WatchedMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> getGreatestWatchedMedias { it.addedOn }
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> getLeastWatchedMedias { it.addedOn }
            else -> flowOf(emptyList())
        }
    }

    private suspend fun deleteWatchedListMediaById(mediaId: String) {
        watchlistRepository.deleteWatchedMediaById(mediaId)
    }

    override fun <R : Comparable<R>> getGreatestWatchedMedias(selector: (WatchedMedia) -> R?): Flow<List<WatchedMedia>> {
        return callbackFlow {
            watchlistRepository.getLatestWatchedMedias().collect { trySend(it.sortedByDescending(selector)) }
        }
    }

    override fun <R : Comparable<R>> getLeastWatchedMedias(selector: (WatchedMedia) -> R?): Flow<List<WatchedMedia>> {
        return callbackFlow {
            watchlistRepository.getLatestWatchedMedias().collect { trySend(it.sortedBy(selector)) }
        }
    }


}