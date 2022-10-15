package com.xcape.movie_logger.presentation.notifications.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.presentation.common.CustomAdapterActions
import com.xcape.movie_logger.presentation.notifications.viewholder.NotificationsViewHolder

class NotificationsAdapter(
    private var notificationsList: MutableList<Notification> = mutableListOf()
) : Adapter<NotificationsViewHolder>(), CustomAdapterActions<Notification> {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NotificationsViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        holder.bind(
            item = getItem(position),
            position = position,
            listener = null
        )
    }

    override fun getItemCount(): Int {
        return notificationsList.size
    }

    override fun getCurrentList(): List<Notification> {
        return notificationsList
    }

    override fun getItem(position: Int): Notification? {
        return notificationsList[position] ?: null
    }

    override fun <T : Any> getItemPositionByProperty(property: T): Int {
        return notificationsList.indexOfFirst { property == it.from?.userId }
    }

    override fun submitList(newList: List<Notification>, isForced: Boolean) {
        val comparator = NotificationsComparator(
            oldList = notificationsList,
            newList = newList
        )
        val diffResult = DiffUtil.calculateDiff(comparator)

        with(notificationsList) {
            clear()
            addAll(newList)
        }

        diffResult.dispatchUpdatesTo(this)
    }

    override fun addItem(position: Int, item: Notification) {
        notificationsList.add(position, item)
        notifyItemInserted(position)
    }

    override fun deleteItem(position: Int) {
        notificationsList.removeAt(position)
        notifyItemRemoved(position)
    }
}

class NotificationsComparator(
    private val oldList: List<Notification>,
    private val newList: List<Notification>
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

        return oldItem.mediaId + oldItem.from?.userId == newItem.mediaId + newItem.from?.userId
    }

}