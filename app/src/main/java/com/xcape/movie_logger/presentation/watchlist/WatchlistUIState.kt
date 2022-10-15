package com.xcape.movie_logger.presentation.watchlist

import com.xcape.movie_logger.domain.model.media.WatchlistMedia

enum class SortType {
    DESCENDING,
    ASCENDING
}

enum class FilterType {
    POPULAR,
    DATE_RELEASED,
    DATE_ADDED
}

enum class ViewType {
    DETAILED,
    COMPACT
}

sealed class WatchlistUIAction {
    data class Sort(
        val sortType: Int,
        val filterType: Int,
        val viewType: Int
    ): WatchlistUIAction()

    data class Filter(val isClicked: Boolean): WatchlistUIAction()

    data class Add(
        val isClicked: Boolean,
        val itemId: String? = null
    ): WatchlistUIAction()

    data class Modify(
        val isClicked: Boolean,
        val itemId: String? = null,
        val itemPosition: Int? = null
    ): WatchlistUIAction()

    data class Delete(
        val isConfirmed: Boolean? = null,
        val trashBin: WatchlistMedia? = null
    ): WatchlistUIAction()
}

data class WatchlistUIState(
    val sortType: Int? = null,
    val filterType: Int? = null,
    val viewType: Int? = null,
    val lastItemUpdated: String? = null,
    val lastItemPositionUpdated: Int? = null,
    val isEditingSortConfig: Boolean = false,
    val isModifyingItem: Boolean = false,
    val isAddingItem: Boolean = false,
)
