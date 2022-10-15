package com.xcape.movie_logger.presentation.profile.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemMediaSmallBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener

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
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .into(mediaImage)

        mediaTitleWithYear.text = titleWithYear

        if(listener == null)
            return

        mediaCard.setOnClickListener {
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