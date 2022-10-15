package com.xcape.movie_logger.presentation.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.presentation.profile.viewholder.ReviewsViewHolder

class ReviewsAdapter : ListAdapter<WatchedMedia, ReviewsViewHolder>(COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ReviewsViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, listener = null)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<WatchedMedia>() {
            override fun areItemsTheSame(oldItem: WatchedMedia, newItem: WatchedMedia): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: WatchedMedia, newItem: WatchedMedia): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

}