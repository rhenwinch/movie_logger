package com.xcape.movie_logger.utils

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture


// https://stackoverflow.com/a/50140418
internal class CallbackFuture : CompletableFuture<Response?>(),
    Callback {

    override fun onFailure(call: Call, e: IOException) {
        super.completeExceptionally(e);
    }

    override fun onResponse(call: Call, response: Response) {
        super.complete(response);
    }
}