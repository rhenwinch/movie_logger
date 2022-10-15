package com.xcape.movie_logger.presentation.watchlist

interface OnSortDialogListener {
    fun onUpdateSortConfig(
        checkedSortType: Int,
        checkedFilterType: Int,
        checkedViewType: Int
    )
}