package com.xcape.movie_logger.presentation.home.viewholder

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ItemUserPostBinding
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.user.Post
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.model.user.WatchStatus
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener

interface PostInteractListener {
    fun onLike(ownerId: String, mediaId: String, position: Int, isDisliking: Boolean = false)
    fun onRate(mediaId: String, position: Int, isUnrating: Boolean)
    fun onWatchlist(mediaId: String, position: Int, isRemoving: Boolean = false)
}

class PostsViewHolder(
    private val binding: ItemUserPostBinding
) : ViewHolder(binding.root) {
    private val profilePictureImageView = binding.profilePicture
    private val usernameTextView = binding.username
    private val postedOnTextView = binding.postedOn
    private val mediaTitleTextView = binding.mediaTitle
    private val mediaImageView = binding.mediaImage
    private val mediaRatingView = binding.mediaRating
    private val mediaReviewTextView = binding.userReview

    private val likesCountRoot = binding.likesCountRoot
    private val likesCount = binding.likesCount
    private val likeButton = binding.likeButton
    private val likeButtonIcon = binding.likeButtonDrawable
    private val likeButtonLabel = binding.likeButtonText
    private val rateButton = binding.rateButton
    private val rateButtonIcon = binding.rateButtonDrawable
    private val rateButtonLabel = binding.rateButtonText
    private val watchlistButton = binding.watchlistButton
    private val watchlistButtonIcon = binding.watchlistButtonDrawable
    private val watchlistButtonLabel = binding.watchlistButtonText

    private var isItemLiked = false
    private var isItemWatchlisted = false
    private var isItemRated = false
    private var likesAmount = 0

    fun bind(
        item: Post?,
        position: Int,
        listener: PostInteractListener?,
        user: User?
    ) {
        if (item == null || user == null)
            return

        likesAmount = item.post!!.likes.size

        // Check if the post is liked
        if(item.isLiked) {
            isItemLiked = true
            modifyLikeButton()
        }

        when(item.watchStatus) {
            WatchStatus.WATCHED -> {
                isItemRated = true
                modifyRateButton()
            }
            WatchStatus.WATCHLIST -> {
                isItemRated = false
                isItemWatchlisted = true
                modifyWatchlistButton()
                modifyRateButton(true)
            }
            WatchStatus.NOT_WATCHED -> {
                isItemWatchlisted = false
                isItemRated = false
                modifyRateButton(true)
                modifyWatchlistButton(true)
            }
        }

        // Show likes count
        showLikesCount(likesAmount)

        if(user.imageProfile != null) {
            Picasso.get()
                .load(user.imageProfile)
                .placeholder(R.drawable.profile_placeholder)
                .fit()
                .centerInside()
                .into(profilePictureImageView)
        }

        usernameTextView.text = user.username
        postedOnTextView.text = parseRelativeTime(item.post.addedOn!!.time)

        Picasso.get()
            .load(item.post.mediaInfo?.gallery?.poster!!.replace("_V1_", "_SL450_"))
            .fit()
            .centerInside()
            .into(mediaImageView)

        mediaRatingView.rating = item.post.rating.toFloat()
        mediaReviewTextView.text = item.post.comments
        mediaTitleTextView.text = item.post.title

        if(listener == null)
            return

        likeButton.setOnSingleClickListener {
            if(isItemLiked) {
                // Disliking Process
                showLikesCount(count = --likesAmount)
            }
            else {
                showLikesCount(count = ++likesAmount)
            }

            listener.onLike(
                ownerId = item.post.ownerId,
                mediaId = item.post.id,
                position = position,
                isDisliking = isItemLiked
            )

            modifyLikeButton(isDisliking = isItemLiked)
        }
        rateButton.setOnSingleClickListener {
            listener.onRate(
                mediaId = item.post.id,
                position = position,
                isUnrating = isItemRated
            )
            modifyRateButton(isUnrating = isItemRated)
        }
        watchlistButton.setOnSingleClickListener {
            listener.onWatchlist(
                mediaId = item.post.id,
                position = position,
                isRemoving = isItemWatchlisted
            )
            modifyWatchlistButton(isRemoving = isItemWatchlisted)
        }
    }

    private fun modifyLikeButton(isDisliking: Boolean = false) {
        isItemLiked = !isDisliking

        likeButtonLabel.text = if(isDisliking) String.format("Like") else String.format("Liked")

        val context = likeButtonIcon.context
        val heartDrawable = if(isDisliking) {
            ContextCompat.getDrawable(context, R.drawable.no_fill_heart)
        } else {
            ContextCompat.getDrawable(context, R.drawable.heart)
        }

        if(!isDisliking)
            heartDrawable?.setTint(ContextCompat.getColor(context, R.color.colorOnError))

        likeButtonIcon.setImageDrawable(heartDrawable)
    }

    private fun modifyRateButton(isUnrating: Boolean = false) {
        rateButtonLabel.text = if(isUnrating) String.format("Rate this") else String.format("Rated")

        val context = rateButtonIcon.context
        val starDrawable = ContextCompat.getDrawable(context, R.drawable.star_2)
        with(starDrawable) {
            this?.let {
                if(isUnrating) {
                    watchlistButton.visibility = View.VISIBLE
                    setTint(ContextCompat.getColor(context, R.color.colorOnMediumEmphasis))
                    isItemRated = false
                }
                else {
                    setTint(ContextCompat.getColor(context, R.color.colorSecondaryLight))
                    watchlistButton.visibility = View.GONE
                    isItemRated = true
                }
            }
        }


        rateButtonIcon.setImageDrawable(starDrawable)
    }

    private fun modifyWatchlistButton(isRemoving: Boolean = false) {
        watchlistButtonLabel.text = if(isRemoving) String.format("Watchlist") else String.format("Watchlisted")

        val context = watchlistButtonIcon.context
        val addDrawable = if(isRemoving) {
            ContextCompat.getDrawable(context, R.drawable.bordered_plus)
        } else {
            ContextCompat.getDrawable(context, R.drawable.check)
        }

        with(addDrawable) {
            this?.let {
                if(isRemoving) {
                    isItemWatchlisted = false
                    setTint(ContextCompat.getColor(context, R.color.colorOnMediumEmphasis))
                } else {
                    isItemWatchlisted = true
                    setTint(ContextCompat.getColor(context, R.color.colorOnSuccess))
                }
            }
        }

        watchlistButtonIcon.setImageDrawable(addDrawable)
    }

    private fun showLikesCount(count: Int) {
        if(count == 1) {
            likesCountRoot.visibility = View.VISIBLE
            likesCount.text = String.format("%d like", count)
        }
        else if(count > 1) {
            likesCountRoot.visibility = View.VISIBLE
            likesCount.text = String.format("%d likes", count)
        }
        else {
            likesCountRoot.visibility = View.GONE
        }
    }

    private fun parseRelativeTime(time: Long): String {
        return DateUtils.getRelativeTimeSpanString(time).toString()
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): PostsViewHolder {
            val binding = ItemUserPostBinding.inflate(inflater, parent, false)
            return PostsViewHolder(binding)
        }
    }
}
