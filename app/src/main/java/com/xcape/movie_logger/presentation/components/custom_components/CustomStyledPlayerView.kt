package com.xcape.movie_logger.presentation.components.custom_components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.google.android.exoplayer2.ui.StyledPlayerView

class CustomStyledPlayerView(context: Context, attrs: AttributeSet?) : StyledPlayerView(context, attrs) {
    var doubleTapDetector: GestureDetector? = null

    constructor(context: Context) : this(context, null)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            doubleTapDetector?.onTouchEvent(event)
            return true
        }
        return super.onTouchEvent(event)
    }
}