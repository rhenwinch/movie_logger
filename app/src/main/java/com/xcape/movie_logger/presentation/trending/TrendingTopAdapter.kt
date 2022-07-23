package com.xcape.movie_logger.presentation.trending

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.presentation.components.setOnSingleClickListener
import java.lang.Exception

class TrendingTopAdapter(
    private val listener: OnMovieClickListener
) : PagingDataAdapter<MediaMetadata, TrendingTopAdapter.TrendingViewHolder>(COMPARATOR) {

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        val media = getItem(position) ?: return
        holder.bind(media, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        return TrendingViewHolder.create(parent)
    }

    class TrendingViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val mediaImageCard: MaterialCardView = itemView.findViewById(R.id.movieImageCard)
        private val mediaImage: ImageView = itemView.findViewById(R.id.movieImage)
        private val mediaTitle: TextView = itemView.findViewById(R.id.movieTitle)

        fun bind(
            item: MediaMetadata,
            listener: OnMovieClickListener
        ) {
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
            mediaImageCard.setOnSingleClickListener {
                listener.onMovieClick(
                    mediaPosition = absoluteAdapterPosition,
                    mediaCategory = "top",
                    mediaId = item.id,
                    mediaImageView = mediaImage,
                    mediaImageCard = mediaImageCard
                )
            }
        }

        companion object {
            fun create(parent: ViewGroup): TrendingViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_small, parent, false)
                return TrendingViewHolder(view)
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<MediaMetadata>() {
            override fun areItemsTheSame(oldItem: MediaMetadata, newItem: MediaMetadata): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: MediaMetadata, newItem: MediaMetadata): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}