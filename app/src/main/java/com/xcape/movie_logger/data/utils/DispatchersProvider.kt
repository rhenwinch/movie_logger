package com.xcape.movie_logger.data.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {
    val io: CoroutineDispatcher
}

class DispatchersProviderImpl : DispatchersProvider {
    override val io: CoroutineDispatcher
        get() = Dispatchers.IO
}