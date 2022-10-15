package com.xcape.movie_logger.presentation.search.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.search.viewholders.SearchResultsViewHolder

class SearchResultsAdapter(
    private val listener: OnMediaClickListener
) : PagingDataAdapter<MediaInfo, SearchResultsViewHolder>(COMPARATOR){
    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int) {
        val searchItem = getItem(position)
        holder.bind(item = searchItem, listener = listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SearchResultsViewHolder.create(inflater, parent)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<MediaInfo>() {
            override fun areItemsTheSame(oldItem: MediaInfo, newItem: MediaInfo): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: MediaInfo, newItem: MediaInfo): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}