package com.xcape.movie_logger.presentation.watched_list.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.presentation.common.CustomAdapterActions
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.watched_list.viewholders.WatchedMediasViewHolder

class WatchedMediasAdapter(
    private val listener: OnMediaClickListener,
    private var watchedList: MutableList<WatchedMedia> = mutableListOf()
) : RecyclerView.Adapter<WatchedMediasViewHolder>(), CustomAdapterActions<WatchedMedia> {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchedMediasViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WatchedMediasViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: WatchedMediasViewHolder, position: Int) {
        if(watchedList.isNotEmpty()) {
            holder.bind(
                item = watchedList[position].mediaInfo,
                listener = listener
            )
        }
    }

    override fun getCurrentList(): List<WatchedMedia> {
        return watchedList
    }

    override fun getItemCount(): Int {
        return if(watchedList.isNotEmpty()) watchedList.size else 0
    }

    override fun getItem(position: Int): WatchedMedia? {
        return if(watchedList.isNotEmpty()) watchedList[position] else null
    }

    override fun submitList(
        newList: List<WatchedMedia>,
        isForced: Boolean
    ) {
        if(watchedList.isEmpty() || isForced) {
            watchedList.addAll(newList)
            notifyItemRangeInserted(0, newList.size)
        }
        // Only capture new submission of lists if it has the same size
        else if(newList.size == watchedList.size) {
            watchedList = newList.toMutableList()
            notifyItemRangeChanged(0, newList.size)
        }
    }

    override fun addItem(
        position: Int,
        item: WatchedMedia
    ) {
        if(watchedList.isNotEmpty()) {
            watchedList.add(position, item)
            notifyItemInserted(position)
        }
    }

    override fun deleteItem(position: Int) {
        if(watchedList.isNotEmpty()) {
            watchedList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, watchedList.size)
        }
    }

    override fun <T : Any> getItemPositionByProperty(property: T): Int {
        return watchedList.indexOfFirst { property == it.id }
    }
}