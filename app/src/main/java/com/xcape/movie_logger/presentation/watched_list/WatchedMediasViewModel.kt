package com.xcape.movie_logger.presentation.watched_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.repository.local.WatchedMediasRepository
import com.xcape.movie_logger.presentation.watchlist.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchedMediasViewModel @Inject constructor(
    private val localRepository: WatchedMediasRepository
) : ViewModel() {
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

        watchedMediaData = getSortedAddedMediasFromWatchedList(SortType.DESCENDING.ordinal)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getSortedPopularMediasFromWatchedList(sortType: Int): Flow<List<WatchedMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> localRepository.getMostPopularWatchedMedias()
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> localRepository.getLeastPopularWatchedMedias()
            else -> flowOf(emptyList())
        }
    }

    private fun getSortedReleasedMediasFromWatchedList(sortType: Int): Flow<List<WatchedMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> localRepository.getRecentlyReleasedWatchedMedias()
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> localRepository.getOldestReleasedWatchedMedias()
            else -> flowOf(emptyList())
        }
    }

    private fun getSortedAddedMediasFromWatchedList(sortType: Int): Flow<List<WatchedMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> localRepository.getLatestWatchedMedias()
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> localRepository.getOldestWatchedMedias()
            else -> flowOf(emptyList())
        }
    }

    private suspend fun deleteWatchedListMediaById(mediaId: String) {
        localRepository.deleteWatchedMediaById(mediaId)
    }
}