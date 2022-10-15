package com.xcape.movie_logger.presentation.trending.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemMediaSmallBinding
import com.xcape.movie_logger.domain.model.media.TopChartMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener

class TopChartViewHolder(
    binding: ItemMediaSmallBinding
) : BaseViewHolder<TopChartMedia>(binding) {
    private val mediaImageCard = binding.mediaImageCard
    private val mediaImage = binding.mediaImage
    private val mediaTitle = binding.mediaTitle

    override fun <ClickListener> bind(
        item: TopChartMedia?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        // Bind text views
        val title = "${item.name} (${item.year})"
        mediaTitle.text = title

        // Bind image views
        Picasso
            .get()
            .load(item.gallery.poster.replace("_V1_", "_SL450_"))
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .fit()
            .into(mediaImage)

        ViewCompat.setTransitionName(mediaImageCard, "${item.id}-top")

        if(listener == null)
            return

        mediaImageCard.setOnSingleClickListener {
            (listener as OnMediaClickListener).onMediaClick(
                mediaCategory = "top",
                mediaId = item.id,
                mediaImageView = mediaImage,
                mediaImageCard = mediaImageCard
            )
        }
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): TopChartViewHolder {
            val binding = ItemMediaSmallBinding.inflate(inflater, parent, false)
            return TopChartViewHolder(binding)
        }
    }
}