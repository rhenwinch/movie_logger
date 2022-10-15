package com.xcape.movie_logger.presentation.trending.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemVpMediaDetailedBinding
import com.xcape.movie_logger.domain.model.media.BoxOfficeMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.domain.utils.Functions

class BoxOfficeViewHolder(
    binding: ItemVpMediaDetailedBinding
) : BaseViewHolder<BoxOfficeMedia>(binding) {
    private val mediaCardView = binding.mediaCardView
    private val mediaImage = binding.mediaImage
    private val mediaTitle = binding.mediaTitle
    private val mediaDuration = binding.mediaDuration
    private val mediaRating = binding.mediaRatingImdb

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): BoxOfficeViewHolder {
            val binding = ItemVpMediaDetailedBinding.inflate(inflater, parent, false)
            return BoxOfficeViewHolder(binding)
        }
    }

    override fun <ClickListener> bind(
        item: BoxOfficeMedia?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        Picasso.get()
            .load(item.gallery.thumbnail!!.replace("_V1_", "_SL750_"))
            .fit()
            .centerInside()
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .into(mediaImage)

        mediaTitle.text = item.name
        mediaDuration.text = Functions.parseDate(item.dateReleased)
        mediaRating.text = item.rating.toString()

        if(listener == null)
            return

        ViewCompat.setTransitionName(mediaCardView, "${item.id}-boxoffice")
        mediaCardView.setOnSingleClickListener {
            (listener as OnMediaClickListener).onMediaClick(
                mediaCategory = "boxoffice",
                mediaId = item.id,
                mediaImageView = mediaImage,
                mediaImageCard = mediaCardView
            )
        }
    }
}