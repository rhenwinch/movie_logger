package com.xcape.movie_logger.presentation.trending.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ItemMediaSmallBinding
import com.xcape.movie_logger.domain.model.media.PopularChartMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import kotlin.math.abs

class PopularChartViewHolder(
    binding: ItemMediaSmallBinding
) : BaseViewHolder<PopularChartMedia>(binding) {
    private val rankChangeContainer = binding.rankChangeLayout
    private val rankChangeArrow = binding.rankChangeIndicator
    private val rankChangeNumber = binding.rankChangeNumber
    private val mediaImageCard = binding.mediaImageCard
    private val mediaImage = binding.mediaImage
    private val mediaTitle = binding.mediaTitle

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): PopularChartViewHolder {
            val binding = ItemMediaSmallBinding.inflate(inflater, parent, false)
            return PopularChartViewHolder(binding)
        }
    }

    override fun <ClickListener> bind(
        item: PopularChartMedia?,
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
            .load(item.image.replace("_V1_", "_SL450_"))
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .fit()
            .into(mediaImage)

        val arrow = if(item.rankChange < 0) {
            R.drawable.down_arrow_2
        }
        else if(item.rankChange > 0) {
            R.drawable.up_arrow_2
        }
        else {
            R.drawable.flat_arrow
        }

        rankChangeContainer.visibility = View.VISIBLE
        rankChangeArrow.setImageResource(arrow)
        rankChangeNumber.text = abs(item.rankChange).toString()

        ViewCompat.setTransitionName(mediaImageCard, "${item.id}-popular")

        if(listener == null)
            return

        mediaImageCard.setOnSingleClickListener {
            (listener as OnMediaClickListener).onMediaClick(
                mediaCategory = "popular",
                mediaId = item.id,
                mediaImageView = mediaImage,
                mediaImageCard = mediaImageCard
            )
        }
    }
}