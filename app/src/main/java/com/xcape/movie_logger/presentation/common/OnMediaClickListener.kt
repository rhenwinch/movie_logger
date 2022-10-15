package com.xcape.movie_logger.presentation.common

import android.widget.ImageView
import com.google.android.material.card.MaterialCardView

interface OnMediaClickListener {
    fun onMediaClick(
        mediaCategory: String,
        mediaId: String,
        mediaImageView: ImageView,
        mediaImageCard: MaterialCardView
    )
}