package com.xcape.movie_logger.presentation.watched_list.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemVpMediaBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener

class WatchedMediasViewHolder(
    binding: ItemVpMediaBinding
) : BaseViewHolder<MediaInfo>(binding) {
    private val mediaCardContainer = binding.mediaCardView
    private val mediaImageView = binding.mediaImage

    override fun <ClickListener> bind(
        item: MediaInfo?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return


        Picasso.get()
            .load(item.gallery.poster.replace("_V1_", "_SL450_"))
            .fit()
            .centerInside()
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .into(mediaImageView)

        if(listener == null)
            return

        ViewCompat.setTransitionName(mediaCardContainer, "${item.id}-watched")
        mediaCardContainer.setOnSingleClickListener {
            (listener as OnMediaClickListener).onMediaClick(
                mediaCategory = "watched",
                mediaId = item.id,
                mediaImageView = mediaImageView,
                mediaImageCard = mediaCardContainer
            )
        }
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): WatchedMediasViewHolder {
            val binding = ItemVpMediaBinding.inflate(inflater, parent, false)
            return WatchedMediasViewHolder(binding)
        }
    }
}