package com.xcape.movie_logger.presentation.trending

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.xcape.movie_logger.domain.model.PopularChart
import com.xcape.movie_logger.presentation.components.setOnSingleClickListener
import java.lang.Exception
import kotlin.math.abs

class TrendingPopularAdapter(
    private val listener: OnMovieClickListener
) : PagingDataAdapter<PopularChart, TrendingPopularAdapter.TrendingPopularViewHolder>(COMPARATOR) {

    override fun onBindViewHolder(holder: TrendingPopularViewHolder, position: Int) {
        val media = getItem(position)?: return
        holder.bind(media, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingPopularViewHolder {
        return TrendingPopularViewHolder.create(parent)
    }

    class TrendingPopularViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val rankChangeContainer: ConstraintLayout = itemView.findViewById(R.id.rankChangeLayout)
        private val rankChangeArrow: ImageView = itemView.findViewById(R.id.rankChangeIndicator)
        private val rankChangeNumber: TextView = itemView.findViewById(R.id.rankChangeNumber)
        private val mediaImageCard: MaterialCardView = itemView.findViewById(R.id.movieImageCard)
        private val mediaImage: ImageView = itemView.findViewById(R.id.movieImage)
        private val mediaTitle: TextView = itemView.findViewById(R.id.movieTitle)

        fun bind(
            item: PopularChart,
            listener: OnMovieClickListener
        ) {

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

            mediaImageCard.setOnSingleClickListener {
                listener.onMovieClick(
                    mediaPosition = absoluteAdapterPosition,
                    mediaCategory = "popular",
                    mediaId = item.id,
                    mediaImageView = mediaImage,
                    mediaImageCard = mediaImageCard
                )
            }
        }

        companion object {
            fun create(parent: ViewGroup): TrendingPopularViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_small, parent, false)
                return TrendingPopularViewHolder(view)
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<PopularChart>() {
            override fun areItemsTheSame(oldItem: PopularChart, newItem: PopularChart): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: PopularChart, newItem: PopularChart): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}