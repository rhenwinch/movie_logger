package com.xcape.movie_logger.presentation.notifications.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ItemNotificationChildNotifBinding
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.presentation.common.BaseViewHolder

class NotificationsViewHolder(
    binding: ItemNotificationChildNotifBinding
) : BaseViewHolder<Notification>(binding) {
    private val profilePicture = binding.profilePicture
    private val username = binding.username
    private val message = binding.message

    override fun <ClickListener> bind(
        item: Notification?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        val profilePictureLink = item.from?.imageProfile

        if (!profilePictureLink.isNullOrEmpty()) {
            Picasso.get()
                .load(profilePictureLink)
                .placeholder(R.drawable.profile_placeholder)
                .fit()
                .centerInside()
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(profilePicture)
        }

        username.text = item.from?.username
        message.text = item.message
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): NotificationsViewHolder {
            val binding = ItemNotificationChildNotifBinding.inflate(inflater, parent, false)
            return NotificationsViewHolder(binding)
        }
    }
}