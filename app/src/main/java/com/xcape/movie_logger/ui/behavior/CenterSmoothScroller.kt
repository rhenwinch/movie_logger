package com.xcape.movie_logger.ui.behavior

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import java.time.Instant
import java.time.ZoneId

class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        val boxCenter = (boxEnd - boxStart) / 2
        val boxScreenCenter = boxStart + boxCenter
        val viewCenter = (viewEnd - viewStart) / 2
        val viewScreenCenter = viewStart + viewCenter
        return boxScreenCenter - viewScreenCenter
    }
}

fun RecyclerView.centerScrollToPosition(context: Context, position: Int) {
    val scroller = CenterSmoothScroller(context)
    scroller.targetPosition = position
    this.layoutManager?.startSmoothScroll(scroller)
}