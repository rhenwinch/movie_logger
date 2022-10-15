package com.xcape.movie_logger.presentation.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.profile.viewholder.WatchlistViewHolder

class WatchlistAdapter(
    private val listener: OnMediaClickListener
) : ListAdapter<WatchlistMedia, WatchlistViewHolder>(COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WatchlistViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item.mediaInfo, listener = listener)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<WatchlistMedia>() {
            override fun areItemsTheSame(oldItem: WatchlistMedia, newItem: WatchlistMedia): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: WatchlistMedia, newItem: WatchlistMedia): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

}