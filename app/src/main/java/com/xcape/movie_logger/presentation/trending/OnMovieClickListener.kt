package com.xcape.movie_logger.presentation.trending

import android.widget.ImageView
import com.google.android.material.card.MaterialCardView

interface OnMovieClickListener {
    fun onMovieClick(
        mediaPosition: Int,
        mediaCategory: String,
        mediaId: String,
        mediaImageView: ImageView,
        mediaImageCard: MaterialCardView
    )
}