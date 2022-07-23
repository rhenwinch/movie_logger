package com.xcape.movie_logger.presentation.trending

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.presentation.components.setOnSingleClickListener
import com.xcape.movie_logger.utils.Functions
import java.lang.Exception

class TrendingBoxOfficeAdapter(
    private val listener: OnMovieClickListener
) : ListAdapter<MediaMetadata, TrendingBoxOfficeAdapter.TrendingBoxOfficeViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingBoxOfficeViewHolder {
        return TrendingBoxOfficeViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TrendingBoxOfficeViewHolder, position: Int) {
        val media = getItem(position)
        holder.bind(media, listener)
    }

    class TrendingBoxOfficeViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val mediaCardView: MaterialCardView = itemView.findViewById(R.id.movieCardView)
        private val mediaImage: ImageView = itemView.findViewById(R.id.movieImage)
        private val mediaTitle: TextView = itemView.findViewById(R.id.movieTitle)
        private val mediaDuration: TextView = itemView.findViewById(R.id.movieDuration)
        private val mediaRating: TextView = itemView.findViewById(R.id.movieRatingImdb)
        private val mediaAdd: ImageButton = itemView.findViewById(R.id.movieAdd)

        fun bind(
            item: MediaMetadata,
            listener: OnMovieClickListener,
        ) {
            Picasso.get()
                .load(item.gallery.thumbnail.replace("_V1_", "_SL750_"))
                .fit()
                .centerInside()
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(mediaImage)

            mediaTitle.text = item.name
            mediaDuration.text = Functions.parseDate(item.dateReleased)
            mediaRating.text = item.rating.toString()

            mediaAdd.setOnSingleClickListener {
                mediaAdd.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.bookmark))
                Toast.makeText(itemView.context, "${item.name} added to watchlist!", Toast.LENGTH_SHORT).show()
            }

            ViewCompat.setTransitionName(mediaCardView, "${item.id}-boxoffice")
            mediaCardView.setOnSingleClickListener {
                listener.onMovieClick(
                    mediaPosition = absoluteAdapterPosition,
                    mediaCategory = "boxoffice",
                    mediaId = item.id,
                    mediaImageView = mediaImage,
                    mediaImageCard = mediaCardView
                )
            }
        }

        companion object {
            fun create(parent: ViewGroup): TrendingBoxOfficeViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_detailed, parent, false)
                return TrendingBoxOfficeViewHolder(view)
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