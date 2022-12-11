package com.xcape.movie_logger.presentation.watchlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.CustomAdapterActions
import com.xcape.movie_logger.presentation.watchlist.ViewType
import com.xcape.movie_logger.presentation.watchlist.WatchlistMediaClickListener
import com.xcape.movie_logger.presentation.watchlist.viewholder.WatchlistCompactViewHolder
import com.xcape.movie_logger.presentation.watchlist.viewholder.WatchlistDetailedViewHolder

class WatchlistAdapter(
    private val type: Int,
    private val listener: WatchlistMediaClickListener,
    private var watchlist: MutableList<WatchlistMedia> = mutableListOf()
) : RecyclerView.Adapter<BaseViewHolder<MediaInfo>>(), CustomAdapterActions<WatchlistMedia> {

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<MediaInfo> {
        val inflater = LayoutInflater.from(parent.context)
        return when(type) {
            ViewType.COMPACT.ordinal -> WatchlistCompactViewHolder.create(inflater, parent)
            ViewType.DETAILED.ordinal -> WatchlistDetailedViewHolder.create(inflater, parent)
            else -> WatchlistDetailedViewHolder.create(inflater, parent)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<MediaInfo>, position: Int) {
        if(watchlist.isNotEmpty()) {
            holder.bind(
                item = watchlist[position].mediaInfo,
                position = position,
                listener = listener
            )
        }
    }

    override fun getItemCount(): Int {
        return if(watchlist.isNotEmpty()) watchlist.size else 0
    }

    override fun getItem(position: Int): WatchlistMedia? {
        return if(watchlist.isNotEmpty()) watchlist[position] else null
    }

    override fun <T : Any> getItemPositionByProperty(property: T): Int {
        return watchlist.indexOfFirst { property == it.id }
    }

    override fun submitList(
        newList: List<WatchlistMedia>,
        isForced: Boolean
    ) {
        if(watchlist.isEmpty() || isForced) {
            watchlist.addAll(newList)
            notifyItemRangeInserted(0, newList.size)
        }
        // Only capture new submission of lists if it has the same size
        else if(newList.size == watchlist.size) {
            watchlist = newList.toMutableList()
            notifyItemRangeChanged(0, newList.size)
        }
    }

    override fun addItem(
        position: Int,
        item: WatchlistMedia
    ) {
        if(watchlist.isNotEmpty()) {
            watchlist.add(position, item)
            notifyItemInserted(position)
        }
    }

    override fun deleteItem(position: Int) {
        if(watchlist.isNotEmpty()) {
            watchlist.removeAt(position)
            notifyItemRemoved(position)
            // notifyItemRangeChanged(0, watchlist.size)
        }
    }

    override fun getCurrentList(): List<WatchlistMedia> {
        return watchlist
    }
}