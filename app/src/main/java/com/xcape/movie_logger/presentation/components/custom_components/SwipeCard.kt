package com.xcape.movie_logger.presentation.components.custom_components

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.home.TopMoviesAdapter

class SwipeCard(
    private val itemList: MutableList<MediaInfo>,
    private val adapter: TopMoviesAdapter
) : ItemTouchHelper.SimpleCallback(0, LEFT) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        if(direction == LEFT) {
            val deletedItem = itemList[position]
            itemList.removeAt(position)
            itemList.add(deletedItem)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemInserted(itemList.size - 1)
        }
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if(viewHolder.absoluteAdapterPosition > 0)
            return 0

        return super.getSwipeDirs(recyclerView, viewHolder)
    }

}