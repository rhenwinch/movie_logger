package com.xcape.movie_logger.presentation.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.presentation.profile.viewholder.FriendsViewHolder

class FriendsAdapter : ListAdapter<User, FriendsViewHolder>(COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FriendsViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, listener = null)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }
        }
    }

}