package com.xcape.movie_logger.presentation.common

import kotlinx.coroutines.flow.Flow

interface MediaSorter<T> {
    fun <R: Comparable<R>>getGreatestWatchedMedias(selector: (T) -> R?): Flow<List<T>>
    fun <R: Comparable<R>>getLeastWatchedMedias(selector: (T) -> R?): Flow<List<T>>
}