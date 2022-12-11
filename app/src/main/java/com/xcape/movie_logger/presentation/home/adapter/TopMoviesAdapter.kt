package com.xcape.movie_logger.presentation.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.presentation.home.viewholder.TopMoviesViewHolder

class TopMoviesAdapter : ListAdapter<WatchedMedia, TopMoviesViewHolder>(COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopMoviesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TopMoviesViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: TopMoviesViewHolder, position: Int) {
        holder.bind(
            item = getItem(position),
            position = position,
            listener = null
        )
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