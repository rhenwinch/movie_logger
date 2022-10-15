package com.xcape.movie_logger.presentation.components.custom_extensions

import android.content.Context
import android.widget.Toast

fun String.asShortToast(context: Context) {
    Toast.makeText(
        context,
        this,
        Toast.LENGTH_SHORT
    ).show()
}

fun String.asLongToast(context: Context) {
    Toast.makeText(
        context,
        this,
        Toast.LENGTH_LONG
    ).show()
}