package com.xcape.movie_logger.presentation.home.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ItemMediaLargeBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder

class TopMoviesViewHolder(private val binding: ItemMediaLargeBinding) : BaseViewHolder<WatchedMedia>(binding) {
    override fun <ClickListener> bind(
        item: WatchedMedia?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        Picasso.get()
            .load(item.mediaInfo!!.gallery.poster.replace("_V1_", "_SL450_"))
            .fit()
            .centerInside()
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .into(binding.movieImage)

    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): TopMoviesViewHolder {
            val binding = ItemMediaLargeBinding.inflate(inflater, parent, false)
            return TopMoviesViewHolder(binding)
        }
    }
}