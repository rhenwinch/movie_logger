package com.xcape.movie_logger.presentation.search.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.common.CustomAdapterActions
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.search.viewholders.SearchResultsViewHolder

class SearchResultsAdapter(
    private val searchResults: MutableList<MediaInfo> = mutableListOf(),
    private val listener: OnMediaClickListener
) : RecyclerView.Adapter<SearchResultsViewHolder>(), CustomAdapterActions<MediaInfo>  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SearchResultsViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int) {
        holder.bind(
            item = getItem(position),
            position = position,
            listener = listener
        )
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun getCurrentList(): List<MediaInfo> {
        return searchResults
    }

    override fun getItem(position: Int): MediaInfo? {
        return searchResults[position] ?: null
    }

    override fun <T : Any> getItemPositionByProperty(property: T): Int {
        return searchResults.indexOfFirst { property == it.id }
    }

    override fun submitList(newList: List<MediaInfo>, isForced: Boolean) {
        val comparator = SearchResultsComparator(
            oldList = searchResults,
            newList = newList
        )
        val diffResult = DiffUtil.calculateDiff(comparator)

        with(searchResults) {
            clear()
            addAll(newList)
        }

        diffResult.dispatchUpdatesTo(this)
    }

    override fun addItem(position: Int, item: MediaInfo) {
        searchResults.add(position, item)
        notifyItemInserted(position)
    }

    override fun deleteItem(position: Int) {
        searchResults.removeAt(position)
        notifyItemRemoved(position)
    }

}

class SearchResultsComparator(
    private val oldList: List<MediaInfo>,
    private val newList: List<MediaInfo>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.id == newItem.id
    }

}