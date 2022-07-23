package com.xcape.movie_logger.presentation.components

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.google.android.exoplayer2.ui.StyledPlayerView

class CustomStyledPlayerView(context: Context, attrs: AttributeSet?) : StyledPlayerView(context, attrs) {
    var doubleTapDetector: GestureDetector? = null

    constructor(context: Context) : this(context, null)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        doubleTapDetector?.onTouchEvent(event)
        return true
    }
}