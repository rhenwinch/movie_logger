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
import com.xcape.movie_logger.domain.utils.Functions.px


class StackingLayoutManager(
    private val context: Context?
) : LayoutManager() {
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

            val movieImageView: ImageView = view.findViewById(R.id.movieImageFront) // The main image view of the layout
            val movieBackButtonBack: ImageButton = view.findViewById(R.id.movieCardBackButton)
            val frontView: MaterialCardView = view.findViewById(R.id.itemMovieLargeFront) // Front view of the itemView
            val backView: MaterialCardView = view.findViewById(R.id.itemMovieLargeBack) // Back view of the itemView

            // If the item is not the first item in the list, descale it.
            if(i > 0) {
                view.scaleX = 1f - (0.1196f * i)
                view.scaleY = 1f - (0.08485f * i)
                movieImageView.foreground = ContextCompat.getDrawable(context!!, R.drawable.item_movie_overlay_strong)
                frontView.setOnLongClickListener(null)
            }
            // If the item is the first one in the list, rescale it back to its original size; and
            // add a onLongClick listener for the preview of the item's information
            else {
                view.scaleX = 1f
                view.scaleY = 1f
                movieImageView.foreground = null

                // Smoothly animate the first item to expand in on the screen/RecyclerView
                val animation = AnimationUtils.loadAnimation(context, R.anim.expand_in)
                view.startAnimation(animation)

                // Attach the onClickListeners to the current view
                bindOnClickListeners(
                    frontView,
                    backView,
                    movieBackButtonBack
                )
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

    private fun bindOnClickListeners(
        frontView: View,
        backView: View,
        movieBackButtonBack: View
    ) {
        // If user long clicks the front view, proceed to flip the itemView horizontally; and
        // Show the back view of the itemView
        frontView.setOnLongClickListener {
            if(backView.visibility == View.GONE) {
                frontView.animate()
                    .rotationY(90F)
                    .setDuration(150)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            frontView.visibility = View.GONE

                            frontView.rotationY = 0F
                            backView.rotationY = -90F
                            backView.visibility = View.VISIBLE

                            backView.animate()
                                .rotationY(0F)
                                .setDuration(150)
                                .setListener(null)
                        }
                    })
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }

        movieBackButtonBack.setOnSingleClickListener {
            backView.animate()
                .rotationY(-90F)
                .setDuration(150)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        backView.visibility = View.GONE

                        backView.rotationY = 0F
                        frontView.rotationY = 90F
                        frontView.visibility = View.VISIBLE

                        frontView.animate()
                            .rotationY(0F)
                            .setDuration(150)
                            .setListener(null)
                    }
                })
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