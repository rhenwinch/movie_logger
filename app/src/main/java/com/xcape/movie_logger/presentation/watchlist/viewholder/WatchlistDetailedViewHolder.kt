package com.xcape.movie_logger.presentation.watchlist.viewholder

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemMediaDetailedBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.watchlist.WatchlistMediaClickListener

class WatchlistDetailedViewHolder(
    binding: ItemMediaDetailedBinding
) : BaseViewHolder<MediaInfo>(binding) {
    private val mediaImage = binding.mediaImagePoster
    private val mediaTitle = binding.mediaTitle
    private val mediaExtraDetails = binding.mediaCertDurationGenre
    private val mediaImdbRating = binding.mediaImdbRating
    private val mediaPersonalRatingLabel = binding.mediaPersonalRatingLabel
    private val mediaPersonalRating = binding.mediaPersonalRating
    private val mediaBriefSypnosis = binding.mediaBriefSypnosis
    private val mediaImagePosterContainer = binding.mediaImagePosterContainer
    private val watchedIndicator = binding.watchedIndicator
    private val parentRootView = binding.root

    override fun <ClickListener> bind(item: MediaInfo?, position: Int?, listener: ClickListener?) {
        if(item == null)
            throw NullPointerException("Invalid media item given!")

        Picasso.get()
            .load(item.gallery.poster.replace("_V1_", "_SL450_"))
            .fit()
            .centerInside()
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .into(mediaImage)

        val notWatched = "Not watched"
        var movieExtraDetails = listOf(item.duration, item.year).joinToString(" | ")
        if(item.certificate != null)
            movieExtraDetails = "${item.certificate} | $movieExtraDetails"

        mediaTitle.text = item.title
        mediaExtraDetails.text = movieExtraDetails
        mediaImdbRating.text = item.rating.toString()
        //mediaPersonalRating.rating = 0F
        mediaPersonalRating.visibility = View.GONE
        mediaPersonalRatingLabel.visibility = View.GONE
        watchedIndicator.text = notWatched
        mediaBriefSypnosis.text = item.plotLong ?: item.plotShort

        if(listener == null)
            return

        ViewCompat.setTransitionName(mediaImagePosterContainer, "${item.id}-watchlist")
        parentRootView.setOnLongClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            (listener as WatchlistMediaClickListener).onAddItemDialogToggle(
                position = position,
                mediaId = item.id
            )
            return@setOnLongClickListener true
        }
        parentRootView.setOnSingleClickListener {
            (listener as WatchlistMediaClickListener).onWatchlistMediaClick(
                mediaCategory = "watchlist",
                mediaId = item.id,
                mediaImageCard = mediaImagePosterContainer,
                mediaImageView = mediaImage
            )
        }
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): WatchlistDetailedViewHolder {
            val binding = ItemMediaDetailedBinding.inflate(inflater, parent, false)
            return WatchlistDetailedViewHolder(binding)
        }
    }
}