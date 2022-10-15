package com.xcape.movie_logger.presentation.components.custom_extensions

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.util.*

//https://gist.github.com/rommansabbir/c56cb28e84bdc499cfd7c63180fb9f95

/**
 * AddToWatchlist an action which will be invoked when the text is changing.
 *
 * @return the [EditText.onTextChangeListener] added to the [EditText]
 */
inline fun EditText.doAfterTextChanged(
    delay: Long = 500,
    crossinline onTextChangedDelayed: (text: String) -> Unit
) = onTextChangeListener(delay, onTextChangedDelayed)


/**
 * AddToWatchlist an action which will be invoked after the text changed.
 *
 * @return the [EditText.onTextChangeListener] added to the [EditText]
 */
inline fun EditText.onTextChangeListener(
    delay: Long,
    crossinline onTextChangedDelayed: (text: String) -> Unit
): TextWatcher {
    val listener  = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            handlerPostDelayed(delay) { onTextChangedDelayed.invoke(s?.toString() ?: "") }
        }
    }
    this.addTextChangedListener(listener)
    return listener
}

var handlerDelayTimer: Timer = Timer()

inline fun handlerPostDelayed(delay: Long, crossinline onSuccess: () -> Unit) {
    handlerDelayTimer.cancel()
    handlerDelayTimer = Timer()
    handlerDelayTimer.schedule(object : TimerTask() {
        override fun run() {
            Handler(Looper.getMainLooper()).post {
                onSuccess.invoke()
            }
        }
    }, delay)
}