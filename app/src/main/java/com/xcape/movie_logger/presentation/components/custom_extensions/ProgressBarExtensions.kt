package com.xcape.movie_logger.presentation.components.custom_extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.xcape.movie_logger.R
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener

fun ProgressBar.activate(container: View? = null) {
    this.scaleX = 1F
    this.scaleY = 1F

    if(container != null) {
        container.visibility = View.VISIBLE
    } else {
        this.visibility = View.VISIBLE
    }
}
fun ProgressBar.deactivate(
    container: View? = null,
    causeIsError: Boolean = false,
    listener: OnLoadingProgressListener? = null
) {
    this.animate()
        .scaleX(0F)
        .scaleY(0F)
        .setDuration(600)
        .setListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if(causeIsError) {
                    val retryViewContainer: LinearLayout = container!!.findViewById(R.id.retryViewContainer)
                    val retryButton: Button = container.findViewById(R.id.retryButton)

                    this@deactivate.visibility = View.GONE
                    retryViewContainer.visibility = View.VISIBLE
                    retryButton.setOnSingleClickListener {
                        retryViewContainer.visibility = View.GONE
                        this@deactivate.activate()
                        listener!!.onRetry()
                    }
                    return
                }
                else {
                    container?.let {
                        it.animate()
                            .alpha(0F)
                            .setDuration(800)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    container.visibility = View.GONE
                                    super.onAnimationEnd(animation)
                                }
                            })
                    }
                }

                super.onAnimationEnd(animation)
            }
        })
}

interface OnLoadingProgressListener {
    fun onRetry()
}