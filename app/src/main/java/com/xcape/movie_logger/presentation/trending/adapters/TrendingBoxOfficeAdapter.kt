package com.xcape.movie_logger.presentation.trending.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.xcape.movie_logger.domain.model.media.BoxOfficeMedia
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.trending.viewholders.BoxOfficeViewHolder

class TrendingBoxOfficeAdapter(
    private val listener: OnMediaClickListener
) : ListAdapter<BoxOfficeMedia, BoxOfficeViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoxOfficeViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BoxOfficeViewHolder.create(layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: BoxOfficeViewHolder, position: Int) {
        val media = getItem(position)
        holder.bind(item = media, listener = listener)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<BoxOfficeMedia>() {
            override fun areItemsTheSame(oldItem: BoxOfficeMedia, newItem: BoxOfficeMedia): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: BoxOfficeMedia, newItem: BoxOfficeMedia): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}