package com.xcape.movie_logger.presentation.profile.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemFriendBinding
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.presentation.common.BaseViewHolder

class FriendsViewHolder(binding: ItemFriendBinding) : BaseViewHolder<User>(binding) {
    private val friendProfileImage = binding.friendProfileImage
    private val friendUsername = binding.friendUsername

    override fun <ClickListener> bind(
        item: User?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        Picasso.get()
            .load(item.imageProfile)
            .fit()
            .centerInside()
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .into(friendProfileImage)

        friendUsername.text = item.username
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): FriendsViewHolder {
            val binding = ItemFriendBinding.inflate(inflater, parent, false)
            return FriendsViewHolder(binding)
        }
    }
}