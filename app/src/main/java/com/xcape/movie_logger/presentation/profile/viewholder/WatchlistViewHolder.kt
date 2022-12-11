package com.xcape.movie_logger.presentation.profile.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemMediaSmallBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener

class WatchlistViewHolder(binding: ItemMediaSmallBinding) : BaseViewHolder<MediaInfo>(binding) {
    private val mediaCard = binding.mediaImageCard
    private val mediaImage = binding.mediaImage
    private val mediaTitleWithYear = binding.mediaTitle

    override fun <ClickListener> bind(
        item: MediaInfo?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        val titleWithYear = "${item.title} (${item.year})"

        Picasso.get()
            .load(item.gallery.poster)
            .fit()
            .centerInside()
            .into(mediaImage)

        mediaTitleWithYear.text = titleWithYear

        if(listener == null)
            return

        mediaCard.setOnSingleClickListener {
            (listener as OnMediaClickListener).onMediaClick(
                mediaCategory = "watchlist",
                mediaId = item.id,
                mediaImageView = mediaImage,
                mediaImageCard = mediaCard
            )
        }
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): WatchlistViewHolder {
            val binding = ItemMediaSmallBinding.inflate(inflater, parent, false)
            return WatchlistViewHolder(binding)
        }
    }
}