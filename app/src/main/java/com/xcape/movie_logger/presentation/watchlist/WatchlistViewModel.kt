package com.xcape.movie_logger.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.repository.remote.WatchlistRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.presentation.common.MediaSorter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val auth: Authenticator,
    private val watchlistRepository: WatchlistRepository
) : ViewModel(), MediaSorter<WatchlistMedia> {

    val state: StateFlow<WatchlistUIState>
    val watchlistData: Flow<List<WatchlistMedia>>
    val accept: (WatchlistUIAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<WatchlistUIAction>()

        val deleteAction = actionStateFlow
            .filterIsInstance<WatchlistUIAction.Delete>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(WatchlistUIAction.Delete()) }

        val sortAction = actionStateFlow
            .filterIsInstance<WatchlistUIAction.Sort>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart {
                val initialState = WatchlistUIAction.Sort(
                    sortType = SortType.DESCENDING.ordinal,
                    filterType = FilterType.DATE_ADDED.ordinal,
                    viewType = ViewType.DETAILED.ordinal
                )
                emit(initialState)
            }

        val filterCardClickAction = actionStateFlow
            .filterIsInstance<WatchlistUIAction.Filter>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(WatchlistUIAction.Filter(isClicked = false)) }

        val isModifying = actionStateFlow
            .filterIsInstance<WatchlistUIAction.Modify>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(WatchlistUIAction.Modify(isClicked = false)) }

        val isAdding = actionStateFlow
            .filterIsInstance<WatchlistUIAction.Add>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(WatchlistUIAction.Add(isClicked = false)) }

        state = combine(
            combine(
                sortAction,
                filterCardClickAction,
                isModifying,
                ::Triple
            ),
            isAdding,
            ::Pair
        ).map { (actionSet1, add) ->
            WatchlistUIState(
                sortType = actionSet1.first.sortType,
                filterType = actionSet1.first.filterType,
                viewType = actionSet1.first.viewType,
                lastItemUpdated = actionSet1.third.itemId,
                lastItemPositionUpdated = actionSet1.third.itemPosition,
                isEditingSortConfig = actionSet1.second.isClicked,
                isModifyingItem = actionSet1.third.isClicked,
                isAddingItem = add.isClicked
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = WatchlistUIState()
        )

        watchlistData = combine(
            sortAction,
            deleteAction,
            ::Pair
        ).flatMapLatest { (sort, delete) ->
            delete.isConfirmed?.let { affirmative ->
                delete.trashBin?.let {
                    if(affirmative) {
                        deleteWatchlistMediaById(delete.trashBin.id)
                    }
                    else {
                        insertWatchlistMedia(delete.trashBin)
                    }
                }
            }

            when(sort.filterType) {
                FilterType.POPULAR.ordinal -> getSortedPopularMediasFromWatchlist(sort.sortType)
                FilterType.DATE_RELEASED.ordinal -> getSortedReleasedMediasFromWatchlist(sort.sortType)
                FilterType.DATE_ADDED.ordinal -> getSortedAddedMediasFromWatchlist(sort.sortType)
                else -> flowOf(emptyList())
            }
        }

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getSortedPopularMediasFromWatchlist(sortType: Int): Flow<List<WatchlistMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> getGreatestWatchedMedias { it.rating }
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> getLeastWatchedMedias { it.rating }
            else -> flowOf(emptyList())
        }
    }

    private fun getSortedReleasedMediasFromWatchlist(sortType: Int): Flow<List<WatchlistMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> getGreatestWatchedMedias { it.dateReleased }
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> getLeastWatchedMedias { it.dateReleased }
            else -> flowOf(emptyList())
        }
    }

    private fun getSortedAddedMediasFromWatchlist(sortType: Int): Flow<List<WatchlistMedia>> {
        return when(sortType) {
            // When chosen sort type is latest
            SortType.DESCENDING.ordinal -> getGreatestWatchedMedias { it.addedOn }
            // When chosen sort type is outdated
            SortType.ASCENDING.ordinal -> getLeastWatchedMedias { it.addedOn }
            else -> flowOf(emptyList())
        }
    }

    private fun deleteWatchlistMediaById(mediaId: String) {
        watchlistRepository.deleteWatchlistMediaById(mediaId)
    }

    private fun insertWatchlistMedia(media: WatchlistMedia) {
        watchlistRepository.insertWatchlistMedia(media)
    }

    override fun <R : Comparable<R>> getGreatestWatchedMedias(selector: (WatchlistMedia) -> R?): Flow<List<WatchlistMedia>> {
        return callbackFlow {
            watchlistRepository.getLatestWatchlistMedias().collect { trySend(it.sortedByDescending(selector)) }
        }
    }

    override fun <R : Comparable<R>> getLeastWatchedMedias(selector: (WatchlistMedia) -> R?): Flow<List<WatchlistMedia>> {
        return callbackFlow {
            watchlistRepository.getLatestWatchlistMedias().collect { trySend(it.sortedBy(selector)) }
        }
    }
}