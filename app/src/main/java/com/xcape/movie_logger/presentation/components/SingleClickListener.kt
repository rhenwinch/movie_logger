package com.xcape.movie_logger.presentation.components

import android.os.SystemClock
import android.view.View
import androidx.core.os.HandlerCompat.postDelayed

class OnSingleClickListener(
    private val block: () -> Unit,
    private val delayTime: Long = 1000
) : View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < delayTime) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        block()
    }
}

fun View.setOnSingleClickListener(
    delayTime: Long = 1000,
    block: () -> Unit
) {
    setOnClickListener(OnSingleClickListener(block, delayTime))
}