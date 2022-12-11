package com.xcape.movie_logger.presentation.components.custom_components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.*
import androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
import com.xcape.movie_logger.R
import kotlin.math.round
import android.view.View
import android.widget.ImageButton
import com.google.android.material.card.MaterialCardView
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.common.Functions.px


class StackingLayoutManager() : LayoutManager() {
    override fun generateDefaultLayoutParams(): LayoutParams
            = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun canScrollHorizontally(): Boolean = false
    override fun canScrollVertically(): Boolean = false

    override fun onLayoutChildren(recycler: Recycler, state: State?) {
        detachAndScrapAttachedViews(recycler);

        for (i in itemCount - 1 downTo 0) {
            val view = recycler.getViewForPosition(i)
            
            // Convert DP size of our item_movie_large layout to PX
            val viewWidth = 234.px
            val viewHeight = 330.px

            // Define the coordinates
            val left = ((round(11.3f * itemCount) * i) + (75 * i)).toInt() + 105
            val right = left + viewWidth
            val top = 0
            val bottom = viewHeight


            //== Start of Applying the layouts for each unique View based on their Indices ==\\
            val movieImageView: ImageView = view.findViewById(R.id.movieImage) // The main image view of the layout

            // If the item is not the first item in the list, descale it.
            if(i > 0) {
                view.scaleX = 1f - (0.1196f * i)
                view.scaleY = 1f - (0.08485f * i)
                movieImageView.foreground = ContextCompat.getDrawable(view.context, R.drawable.item_movie_overlay_strong)
            }
            // If the item is the first one in the list, rescale it back to its original size; and
            else {
                view.scaleX = 1f
                view.scaleY = 1f
                movieImageView.foreground = null
            }
            //== End of Applying the layouts for each unique View based on their Indices ==\\

            // AddToWatchlist the view to the RecyclerView and add layout their coordinates
            addView(view)
            measureChild(view, viewWidth - (28 * i),  viewHeight - (28 * i))
            layoutDecorated(view, left, top, right, bottom)
        }

        val scrapListCopy = recycler.scrapList.toList()
        scrapListCopy.forEach {
            recycler.recycleView(it.itemView)
        }
    }


    //private val mShrinkAmount = 0.15f;
    //private val mShrinkDistance = 0.2f;
    //override fun scrollHorizontallyBy(
    //    dx: Int,
    //    recycler: Recycler?,
    //    state: State?
    //): Int {
    //    val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
    //    val midpoint = 247f
    //    val d0 = 0f
    //    val d1 = mShrinkDistance + midpoint
    //    val s0 = 1f
    //    val s1 = 1f - mShrinkAmount
    //
    //    for (i in 0 until childCount) {
    //        val child = getChildAt(i)
    //        val childEdgeSize = getDecoratedRight(child!!) + getDecoratedLeft(child)
    //        var childMidPoint = ((childEdgeSize) / 2f)
    //        val d = d1.coerceAtMost(abs(midpoint - childMidPoint))
    //        val scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0)
    //        child.scaleX = scale
    //        child.scaleY = scale
    //
    //
    //        println("$width == $midpoint == $scale == ${childMidPoint} == $d")
    //    }
    //    return scrolled
    //}
}