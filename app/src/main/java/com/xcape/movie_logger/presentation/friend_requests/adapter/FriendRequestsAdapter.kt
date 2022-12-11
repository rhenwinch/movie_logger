package com.xcape.movie_logger.presentation.friend_requests.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.presentation.common.CustomAdapterActions
import com.xcape.movie_logger.presentation.friend_requests.viewholder.FriendRequestsViewHolder

class FriendRequestsAdapter(
    private var friendRequestsList: MutableList<FriendRequest> = mutableListOf()
) : Adapter<FriendRequestsViewHolder>(), CustomAdapterActions<FriendRequest> {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FriendRequestsViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: FriendRequestsViewHolder, position: Int) {
        holder.bind(
            item = getItem(position),
            position = position,
            listener = null
        )
    }

    override fun getItemCount(): Int {
        return friendRequestsList.size
    }

    override fun getCurrentList(): List<FriendRequest> {
        return friendRequestsList
    }

    override fun getItem(position: Int): FriendRequest? {
        return friendRequestsList[position] ?: null
    }

    override fun <T : Any> getItemPositionByProperty(property: T): Int {
        return friendRequestsList.indexOfFirst { property == it.fromUserId }
    }

    override fun submitList(newList: List<FriendRequest>, isForced: Boolean) {
        val comparator = FriendRequestComparator(
            oldList = friendRequestsList,
            newList = newList
        )
        val diffResult = DiffUtil.calculateDiff(comparator)

        with(friendRequestsList) {
            clear()
            addAll(newList)
        }

        diffResult.dispatchUpdatesTo(this)
    }

    override fun addItem(position: Int, item: FriendRequest) {
        friendRequestsList.add(position, item)
        notifyItemInserted(position)
    }

    override fun deleteItem(position: Int) {
        friendRequestsList.removeAt(position)
        notifyItemRemoved(position)
    }
}

class FriendRequestComparator(
    private val oldList: List<FriendRequest>,
    private val newList: List<FriendRequest>
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

        return oldItem.fromUserId == newItem.fromUserId
    }

}